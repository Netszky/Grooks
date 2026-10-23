# ADR-001 : Monolithe découpé par fonctionnalité

- **Statut** : accepté
- **Date** : 2026-10-23

## Contexte

Grooks est développé par une seule personne, à raison d'environ 8 heures par semaine. L'objectif immédiat est un premier noyau fonctionnel : comptes, cours récurrents, réservations et liste d'attente.

Les domaines métier sont fortement liés. Réserver une place modifie la disponibilité d'une séance ; une annulation déclenche la promotion de la liste d'attente ; l'annulation d'une séance par un coach annule toutes ses réservations. Ces opérations doivent rester cohérentes, idéalement au sein d'une même transaction.

Le déploiement doit rester simple : un seul serveur modeste et une seule chaîne d'intégration continue.

Une expérience précédente sur une architecture microservices (découverte de services avec Eureka, gateway) a montré le coût de ce modèle pour une petite équipe : plusieurs services à lancer et à configurer, des appels réseau entre services et des données réparties.

## Options envisagées

### 1. Microservices

Un service par domaine (membres, cours, réservations), chacun avec sa base.

- ➕ Déploiement et montée en charge indépendants par domaine
- ➖ Complexité opérationnelle élevée (gateway, découverte de services, observabilité distribuée)
- ➖ Une réservation devient une opération distribuée : plus de transaction locale, cohérence à gérer entre services
- ➖ Disproportionné pour un développeur seul et un premier noyau

### 2. Monolithe organisé en couches techniques

Des packages `controller/`, `service/` et `repository/` qui regroupent tous les domaines.

- ➕ Simple à démarrer, organisation très répandue
- ➖ Les domaines sont mélangés : rien ne montre où s'arrête la réservation et où commencent les cours
- ➖ Couplage qui grandit sans être vu, et extraction d'un domaine difficile plus tard

### 3. Monolithe découpé par fonctionnalité

Une seule application, mais des packages par domaine métier (`member/`, `course/`, `booking/`, `common/`), chacun contenant son controller, son service, son repository et ses entités.

- ➕ Un seul déploiement et des transactions locales
- ➕ Des frontières de domaine visibles dans le code
- ➕ Un domaine peut être extrait en service plus tard si un vrai besoin apparaît
- ➖ Les frontières ne tiennent que si elles sont respectées (voir Conséquences)

## Décision

L'option 3 est retenue : un **monolithe découpé par fonctionnalité**.

Elle répond aux contraintes actuelles (un développeur, un premier noyau, des opérations qui doivent rester cohérentes) sans fermer la porte à une évolution. Les microservices résoudraient des problèmes que le projet n'a pas : une forte charge, plusieurs équipes, des rythmes de déploiement différents.

## Conséquences

**Positives**

- Une seule application à lancer, tester et déployer
- La réservation, la liste d'attente et la disponibilité des séances se gèrent dans des transactions locales
- Le code se lit par domaine métier, dans le même vocabulaire que les user stories

**Négatives**

- Toute l'application se déploie et monte en charge d'un bloc
- Rien n'empêche techniquement un domaine d'utiliser directement les classes internes d'un autre

**Règles adoptées pour compenser**

- Chaque domaine expose à ses voisins **uniquement** son service public ; ses repositories et ses entités restent internes au package
- Pas d'accès direct à la base d'un autre domaine : on passe par son service
- Piste pour plus tard : vérifier ces règles automatiquement par un test, avec Spring Modulith ou ArchUnit
