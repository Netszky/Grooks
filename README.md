# Grooks

Application de réservation de cours collectifs pour salles de sport et coachs indépendants.

> 🚧 **Projet en cours de construction.** Les fonctionnalités ci-dessous décrivent le périmètre visé ; leur avancement est suivi dans les [issues](https://github.com/Netszky/Grooks/issues) et la [feuille de route](#feuille-de-route).

## Pourquoi ce projet

Grooks est un projet personnel construit pour montrer **tout le cycle de vie d'une application**, pas seulement le code :

- **Analyse** : besoins exprimés en user stories avec critères d'acceptation, backlog organisé en milestones et en epics.
- **Développement** : un backend Java / Spring Boot testé, puis un front Angular.
- **DevOps** : intégration continue, conteneurisation, déploiement.
- **Sécurité applicative** : authentification, contrôle d'accès et bonnes pratiques intégrés dès la conception.

Le domaine a été choisi pour ses vrais défis techniques : empêcher la surréservation quand deux personnes réservent la dernière place au même moment, gérer une liste d'attente qui se libère automatiquement, générer des séances récurrentes.

## Fonctionnalités visées (premier noyau)

Trois rôles : **membre**, **coach** et **gérant**.

- Inscription, connexion et gestion des rôles
- Cours récurrents (par exemple « Yoga, tous les mardis à 18 h, 12 places ») et génération des séances
- Réservation à capacité limitée, sans surréservation
- Liste d'attente avec promotion automatique en cas d'annulation
- Annulation par un membre ou par un coach, avec notification des personnes concernées
- Rappels automatiques avant une séance
- Statistiques de fréquentation pour le gérant

## Comment le projet est mené

| Domaine | Pratiques |
|---|---|
| Analyse | User stories et critères d'acceptation en issues GitHub, epics et sous-issues, milestones, tableau de suivi |
| Architecture | Monolithe découpé par fonctionnalité ; décisions documentées en [ADR](docs/adr) |
| Développement | Branche et pull request par issue, messages de commit conventionnels, tests unitaires et tests d'intégration sur une vraie base PostgreSQL (Testcontainers) |
| DevOps | CI GitHub Actions à chaque pull request, Docker et Docker Compose, configuration par variables d'environnement, déploiement |
| Sécurité | JWT, mots de passe hashés avec BCrypt, contrôle d'accès par rôle et par propriétaire (prévention des IDOR), erreurs génériques qui ne révèlent rien, secrets hors du dépôt, surveillance des dépendances |

## Stack technique

- **Backend** : Java 25, Spring Boot 4, Spring Data JPA, Spring Security
- **Base de données** : PostgreSQL 18, migrations Flyway
- **Tests** : JUnit 5, Mockito, Testcontainers
- **Outillage** : Maven, Docker, GitHub Actions
- **Front** (à venir) : Angular

## Démarrer en local

**Prérequis** : Java 25 et Docker.

```bash
# 1. Configuration locale
cp .env.example .env
# puis générer un secret JWT et le placer dans .env :
openssl rand -base64 32

# 2. Base de données
docker compose up -d

# 3. Application
./mvnw spring-boot:run

# 4. Vérification
curl http://localhost:8080/actuator/health
```

**Lancer les tests** (Docker doit être démarré, Testcontainers lance sa propre base PostgreSQL) :

```bash
./mvnw test
```

## Feuille de route

- [ ] **Fondation** : initialisation du projet, CI, Testcontainers, gestion des erreurs, authentification
- [ ] **Cours et séances** : cours récurrents, gestion par les coachs et le gérant
- [ ] **Réservation et liste d'attente** : réservation sans surréservation, promotion automatique, annulations
- [ ] **Espace gérant** : statistiques de fréquentation
- [ ] **Front Angular**
- [ ] **Déploiement** et démo en ligne
