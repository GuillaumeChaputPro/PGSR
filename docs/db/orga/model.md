# Modèle de données

## Vue d'ensemble

Le modèle est organisé en deux domaines :
- **Business** : hiérarchie organisationnelle et géographique des sites
- **Sécurité** : gestion des utilisateurs, rôles et périmètres d'accès

---

## Domaine Business

### Tables principales

| Table | Description | Clé primaire |
|-------|-------------|--------------|
| **GROUP** | Groupe corporate (ex: Accor) | `id` (UUID) |
| **BRAND** | Marques (Ibis, Novotel, Sofitel...) | `id` (UUID) |
| **REGION** | Continents (Europe, Asia, Americas...) | `id` (UUID) |
| **COUNTRY** | Pays (ISO alpha-2) | `code` (char) |
| **SITE** | Hôtels, restaurants, établissements | `id` (UUID) |

### Hiérarchies

**Organisationnelle :** `GROUP → BRAND → SITE`

**Géographique :** `REGION → COUNTRY → SITE`

Un SITE appartient à **une marque** et **un pays**. Une marque peut être présente dans plusieurs pays.

---

## Domaine Sécurité

### Concepts clés

Un **utilisateur** reçoit un **rôle** (ce qu'il peut faire) avec un **périmètre** (où il peut le faire).

### Tables de sécurité

| Table | Description |
|-------|-------------|
| **USER** | Compte utilisateur |
| **ROLE** | Rôle système (ADMIN, MANAGER, VIEWER, AUDITOR) |
| **USER_ASSIGNMENT** | Affectation d'un rôle à un utilisateur |
| **ASSIGNMENT_ORG_SCOPE** | Périmètre organisationnel (GROUP, BRAND) |
| **ASSIGNMENT_GEO_SCOPE** | Périmètre géographique (REGION, COUNTRY) |
| **ASSIGNMENT_SITE_SCOPE** | Périmètre site spécifique (SITE) |

### Fonctionnement

```
USER_ASSIGNMENT = USER + ROLE + (ORG_SCOPE × GEO_SCOPE) ∪ SITE_SCOPE
```

- Une affectation peut avoir **0 à N scopes organisationnels**
- Une affectation peut avoir **0 à N scopes géographiques**
- Une affectation peut avoir **0 à N scopes sites**
- **Pas de scope = accès global** (pour les admins)
- Les scopes **cascadent automatiquement** (Europe → tous les pays européens, Pays → tous les sites du pays)
- **Logique d'union** : SITE_SCOPE s'**ajoute** aux accès définis par ORG_SCOPE × GEO_SCOPE (pas de remplacement)

---

## Rôles disponibles

| Rôle | Permissions typiques |
|------|---------------------|
| **ADMIN** | Gestion complète : utilisateurs, sites, configuration |
| **MANAGER** | Créer/modifier sites, générer rapports, gérer équipes |
| **VIEWER** | Consultation uniquement (dashboards, listes) |
| **AUDITOR** | Lecture + export données pour audit |

---

## Exemples de combinaisons

### Exemple 1 : Manager Europe pour Ibis
```
USER: john.doe@accor.com
ROLE: MANAGER
ORG_SCOPE: BRAND = Ibis
GEO_SCOPE: REGION = Europe
```
→ John peut gérer tous les sites Ibis en Europe

---

### Exemple 2 : Viewer France multi-marques
```
USER: marie.martin@accor.com
ROLE: VIEWER
ORG_SCOPE: BRAND = Novotel
ORG_SCOPE: BRAND = Mercure
GEO_SCOPE: COUNTRY = FR
```
→ Marie voit uniquement les Novotel et Mercure français

---

### Exemple 3 : Auditor Luxury global
```
USER: auditor@accor.com
ROLE: AUDITOR
ORG_SCOPE: BRAND = Sofitel
ORG_SCOPE: BRAND = Pullman
ORG_SCOPE: BRAND = MGallery
(Pas de GEO_SCOPE)
```
→ Accès à toutes les marques Luxury dans le monde entier

---

### Exemple 4 : Admin restreint Amérique du Nord
```
USER: admin.na@accor.com
ROLE: ADMIN
GEO_SCOPE: REGION = North_America
(Pas d'ORG_SCOPE)
```
→ Administration complète pour toutes les marques en Amérique du Nord

---

### Exemple 5 : Manager mono-site
```
USER: hotel.manager@accor.com
ROLE: MANAGER
SITE_SCOPE: SITE = Ibis Paris Bastille (id: abc-123)
```
→ Gestion d'un seul hôtel spécifique

---

### Exemple 6 : Viewer multi-sites sélectionnés
```
USER: regional.viewer@accor.com
ROLE: VIEWER
SITE_SCOPE: SITE = Novotel Paris Tour Eiffel
SITE_SCOPE: SITE = Novotel Paris Les Halles
SITE_SCOPE: SITE = Mercure Paris Opéra
```
→ Vue sur 3 hôtels spécifiques uniquement

---

### Exemple 7 : Manager France + sites spécifiques à l'étranger
```
USER: cross.border.manager@accor.com
ROLE: MANAGER
GEO_SCOPE: COUNTRY = FR
SITE_SCOPE: SITE = Ibis Rome Termini
SITE_SCOPE: SITE = Novotel Milano Centro
```
→ Gestion de tous les sites français + 2 hôtels italiens spécifiques

---

## Matrice de combinaisons

| Dimension | Scope vide | GROUP | BRAND | REGION | COUNTRY | SITE |
|-----------|------------|-------|-------|--------|---------|------|
| **Vide** | Global | Tout le groupe | Marque mondiale | Continent entier | Pays entier | Sites listés |
| **GROUP** | N/A | N/A | N/A | Groupe dans région | Groupe dans pays | Groupe + sites |
| **BRAND** | Marque mondiale | N/A | N/A | Marque dans région | Marque dans pays | Marque + sites |
| **REGION** | Continent entier | Groupe dans région | Marque dans région | N/A | N/A | Région + sites |
| **COUNTRY** | Pays entier | Groupe dans pays | Marque dans pays | N/A | N/A | Pays + sites |
| **SITE** | Sites spécifiques | Sites + groupe | Sites + marque | Sites + région | Sites + pays | Sites listés |

**Principe clé** : Les différents types de scopes s'**additionnent** (union). Un utilisateur avec COUNTRY=FR et SITE=Rome a accès à tous les sites français PLUS le site romain.

---

## Règles de gestion

1. **Cascade automatique** : Europe inclut automatiquement FR, DE, IT, ES... / Pays inclut tous ses sites
2. **Intersection logique ORG × GEO** : ORG_SCOPE **ET** GEO_SCOPE (pas OU)
3. **Multi-scope additif** : Plusieurs scopes du même type = union
4. **Union des dimensions** : (ORG × GEO) **OU** SITE - les accès s'additionnent
5. **Admin sans scope** : accès total au système
6. **Validation** : Au moins un scope requis (sauf ADMIN global)

---

## Cas particuliers

### Accès global par organisation
```
ROLE: VIEWER
ORG_SCOPE: GROUP = Accor
(Pas de GEO_SCOPE)
```
→ Voir tous les sites du groupe dans le monde

### Accès multi-régional
```
ROLE: MANAGER
ORG_SCOPE: BRAND = Ibis
GEO_SCOPE: REGION = Europe
GEO_SCOPE: REGION = Asia
```
→ Gérer les Ibis en Europe et Asie (pas Amériques/Afrique)

### Accès croisé complexe
```
ROLE: AUDITOR
ORG_SCOPE: BRAND = Novotel
ORG_SCOPE: BRAND = Mercure
GEO_SCOPE: COUNTRY = FR
GEO_SCOPE: COUNTRY = BE
GEO_SCOPE: COUNTRY = CH
```
→ Audit des Novotel et Mercure en France, Belgique et Suisse uniquement

### Accès à une liste de sites hétérogènes
```
ROLE: MANAGER
SITE_SCOPE: SITE = Ibis Paris Bastille
SITE_SCOPE: SITE = Novotel Lyon Centre
SITE_SCOPE: SITE = Mercure Marseille Vieux Port
```
→ Gérer 3 hôtels spécifiques de marques et villes différentes

### Accès hybride géographique + sites spécifiques
```
ROLE: VIEWER
ORG_SCOPE: BRAND = Ibis
GEO_SCOPE: COUNTRY = FR
SITE_SCOPE: SITE = Ibis Brussels Centre
SITE_SCOPE: SITE = Ibis Amsterdam Central
```
→ Voir tous les Ibis français + 2 Ibis spécifiques en Belgique et Pays-Bas

---

## Questions fréquentes

**Q : Un utilisateur peut-il avoir plusieurs rôles ?**  
A : Oui, via plusieurs USER_ASSIGNMENT. Exemple : MANAGER + AUDITOR.

**Q : Comment donner accès à un seul hôtel ?**  
A : Utiliser ASSIGNMENT_SITE_SCOPE avec l'ID du site spécifique.

**Q : Peut-on combiner SITE_SCOPE avec ORG_SCOPE ou GEO_SCOPE ?**  
A : Oui ! Les accès s'additionnent (union). Exemple : COUNTRY=FR + SITE=Rome → accès à tous les sites français plus le site romain.

**Q : Si j'ai ORG_SCOPE=Ibis + GEO_SCOPE=Europe + SITE_SCOPE=Novotel Tokyo ?**  
A : L'utilisateur aura accès à tous les Ibis européens (intersection ORG×GEO) PLUS le Novotel Tokyo (union avec SITE).

**Q : Que se passe-t-il sans aucun scope ?**  
A : Accès global (réservé aux ADMIN). Pour les autres rôles, au moins un scope est requis.

**Q : Les scopes expirent-ils ?**  
A : Non, pas dans la version actuelle. Ajouter `valid_from`/`valid_until` si nécessaire.

---

## Évolutions futures possibles

- Ajout d'un niveau CITY entre COUNTRY et SITE
- Permissions granulaires par feature (lecture/écriture/export/delete)
- Scopes temporaires avec dates d'expiration
- Délégation de droits (un manager peut créer des viewers)
- Groupes de sites prédéfinis (ex: "Sites Premium Paris")
