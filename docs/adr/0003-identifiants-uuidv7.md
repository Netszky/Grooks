# ADR-003 : Identifiants UUIDv7

- **Statut** : accepté
- **Date** : 2026-11-03

## Contexte

Chaque entité (utilisateur, cours, séance, réservation) a besoin d'une clé primaire. Ces identifiants apparaissent dans les URL de l'API (`/courses/{id}`) et dans le token JWT (`sub`).

Contraintes :

- un identifiant exposé ne doit pas permettre de **deviner les autres** (énumération, IDOR) ni de révéler le volume d'activité ;
- les insertions doivent rester performantes sur un index B-tree PostgreSQL ;
- le choix doit être fait dès le départ : changer le type de clé plus tard impose de migrer toutes les tables et clés étrangères.

## Options envisagées

### 1. Entier auto-incrémenté (`BIGSERIAL`)

- ➕ Compact (8 octets), lisible, très performant
- ➖ **Prévisible** : `/users/42` invite à essayer `/users/43`. Un oubli de contrôle d'accès devient immédiatement exploitable
- ➖ Révèle le volume : un id 1 200 indique environ 1 200 inscrits

### 2. UUID version 4 (aléatoire)

- ➕ Impossible à deviner (122 bits aléatoires)
- ➕ Génération possible côté application, sans aller-retour en base
- ➖ Valeurs totalement aléatoires : chaque insertion tombe n'importe où dans l'index, ce qui le fragmente et dégrade les performances quand la table grossit

### 3. UUID version 7 (horodaté puis aléatoire)

- ➕ Impossible à deviner en pratique (74 bits aléatoires)
- ➕ Les 48 premiers bits sont un horodatage : les ids sont **croissants dans le temps**, les insertions se font en fin d'index comme avec un entier
- ➕ Supporté nativement par PostgreSQL 18 (`uuidv7()`) et Hibernate (`@UuidGenerator(style = VERSION_7)`)
- ➖ 16 octets au lieu de 8
- ➖ L'horodatage révèle la **date de création** de la ressource

## Décision

L'option 3 est retenue : des **UUIDv7** pour toutes les clés primaires.

- En Java, l'id est généré par Hibernate avec `@UuidGenerator(style = UuidGenerator.Style.VERSION_7)` et typé `UUID` (jamais `String`) dans tout le code.
- En base, la colonne est de type `UUID` avec `DEFAULT uuidv7()`, pour que les insertions faites hors de l'application (scripts, migrations de données) suivent la même règle.

## Conséquences

**Positives**

- Les identifiants exposés ne permettent ni l'énumération ni l'estimation du volume
- Performances d'insertion proches d'un entier, sans fragmentation de l'index
- Typage fort côté Java : impossible de passer un email ou un autre texte à la place d'un id

**Négatives**

- Clés et index deux fois plus gros qu'avec un `BIGINT` ; négligeable au volume visé
- Ids moins lisibles pour le debug et dans les URL
- La date de création se lit dans l'id ; acceptable pour les données de Grooks

**À retenir**

Un identifiant impossible à deviner **ne remplace pas** le contrôle d'accès : chaque route qui reçoit un id doit vérifier que l'appelant a le droit d'accéder à la ressource. L'UUID limite seulement les dégâts en cas d'oubli.
