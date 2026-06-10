# Smart Farm Management Project: From Zero to One Hundred

## 1. What This Project Is

This is a Java console application for managing an intelligent farm. It models a complete farm domain:

- farm zones
- crops
- animals
- sensors
- readings
- alerts
- production history
- dashboards
- import from text files

The program is menu-driven and runs entirely in the terminal. The user creates or loads a `Ferme` instance, then interacts with the domain through menus in `ferme.Main`.

At a high level, the project is an object-oriented domain model wrapped in a text UI.

## 2. Entry Point And Startup Flow

The application starts in the top-level [Main.java](Main.java), which is only a launcher:

```java
public class Main {
    public static void main(String[] args) {
        ferme.Main.main(args);
    }
}
```

So the real application logic lives in [ferme/Main.java](ferme/Main.java).

Startup flow:

1. The JVM launches [Main.java](Main.java).
2. That delegates directly to [ferme.Main](ferme/Main.java).
3. `ferme.Main` prints the welcome screen and main menu.
4. The user creates a farm or imports data.
5. The user then manages zones, crops, animals, sensors, readings, alerts, and dashboards.

## 3. Main Architecture

The code is split into three layers.

### 3.1 Console Layer

This is [ferme/Main.java](ferme/Main.java).

It handles:

- user input
- menus
- input validation loops
- calling domain methods
- converting raw console input into objects

It does not contain farm business rules itself. It is an orchestration layer.

### 3.2 Domain Layer

This is [ferme/Ferme.java](ferme/Ferme.java) and the classes under [ferme/models](ferme/models).

It contains the real behavior:

- storing zones, sensors, and alerts
- attaching entities together
- registering readings
- generating alerts
- updating animal positions from GPS readings
- rendering dashboards and reports

### 3.3 Validation And Parsing Layer

This is [ferme/ValidationUtils.java](ferme/ValidationUtils.java) and [ferme/ImporteurDonnees.java](ferme/ImporteurDonnees.java).

It handles:

- date validation
- range validation
- file import parsing
- turning text rows into domain objects

## 4. Core Data Model

The farm is represented by a set of connected objects.

### 4.1 Ferme

[ferme/Ferme.java](ferme/Ferme.java) is the central aggregate.

It owns:

- `nomFerme`
- `Map<String, Zone> zones`
- `Map<String, Capteur> capteurs`
- `List<Alerte> alertes`

This means the farm is the central source of truth. Most operations start from `Ferme`.

### 4.2 Zones

All zones inherit from [ferme/models/Zone.java](ferme/models/Zone.java).

Common zone state:

- code
- name
- status (`ACTIVE` or `SUSPENDUE`)
- production history
- associated sensors

Common zone behavior:

- activate / suspend
- record production
- attach or detach sensors
- display details

There are three zone subtypes:

- [ZoneCulture](ferme/models/ZoneCulture.java)
- [ZoneElevage](ferme/models/ZoneElevage.java)
- [ZoneAquacole](ferme/models/ZoneAquacole.java)

#### ZoneCulture

Stores a list of `Culture` objects.

It supports:

- adding a culture
- reading a culture by index
- counting cultures
- generating a culture report by family and growth stage

#### ZoneElevage

Stores:

- the livestock type (`RUMINANT` or `VOLAILLE`)
- animals in a map keyed by animal number
- an optional feeding program
- the geographic center of the zone
- a radius in meters

It supports:

- adding animals if the animal type matches the zone type
- searching an animal by number
- listing animals by health status
- showing the feeding program

#### ZoneAquacole

Stores:

- aquatic species
- an optional feeding program

It supports:

- adding species
- counting total animals across species
- showing the species list and feeding program

### 4.3 Sensors

All sensors inherit from [ferme/models/Capteur.java](ferme/models/Capteur.java).

Common sensor state:

- code
- zone code
- status (`ACTIF`, `SUSPENDU`, `DEFAILLANT`)
- min and max thresholds
- a reading history

Common sensor behavior:

- suspend / reactivate
- evaluate a reading
- store a reading
- print reading history

There are five sensor families:

- [CapteurEnvironnemental](ferme/models/CapteurEnvironnemental.java)
- [CapteurSol](ferme/models/CapteurSol.java)
- [CapteurBiometrique](ferme/models/CapteurBiometrique.java)
- [CapteurGPS](ferme/models/CapteurGPS.java)
- [CapteurEau](ferme/models/CapteurEau.java)

#### Numeric sensors

Environmental, soil, biometric, and water sensors use thresholds.

They all use the base numeric evaluation logic in `Capteur`:

- inside range -> `NORMAL`
- outside range -> `AVERTISSEMENT`
- far outside range -> `CRITIQUE`

The project computes severity using the distance from the threshold range relative to the total range.

#### GPS sensors

[CapteurGPS](ferme/models/CapteurGPS.java) is special.

It stores:

- the linked animal number
- a reference center position
- a radius

When a GPS reading arrives:

- if the position is inside the radius -> `NORMAL`
- if it exceeds the radius -> `AVERTISSEMENT`
- if it exceeds 1.5 times the radius -> `CRITIQUE`

### 4.4 Readings

All readings inherit from [ferme/models/Releve.java](ferme/models/Releve.java).

Common reading fields:

- date
- sensor code
- severity level

Subtypes:

- [ReleveNumerique](ferme/models/ReleveNumerique.java)
- [ReleveGPS](ferme/models/ReleveGPS.java)

#### ReleveNumerique

Represents a numeric value with a unit.

Example:

- temperature
- pH
- oxygen level

#### ReleveGPS

Represents a geographic position.

### 4.5 Alerts

[ferme/models/Alerte.java](ferme/models/Alerte.java) stores one alert generated from a reading.

Alert fields:

- auto-incremented ID
- linked reading
- severity
- zone code
- acknowledged flag
- creation date

Alerts are created automatically when a reading is not `NORMAL`.

### 4.6 Production History

[ferme/models/HistoriqueProduction.java](ferme/models/HistoriqueProduction.java) stores production entries for a zone.

Each entry contains:

- date
- value
- unit

It can also compute the average production.

### 4.7 Farm Entities

Other important domain objects:

- [Animal](ferme/models/Animal.java)
- [Culture](ferme/models/Culture.java)
- [EspeceAquacole](ferme/models/EspeceAquacole.java)
- [ProgrammeAlimentation](ferme/models/ProgrammeAlimentation.java)
- [ExigencesPedologiques](ferme/models/ExigencesPedologiques.java)
- [PositionGPS](ferme/models/PositionGPS.java)

#### Animal

Stores:

- animal number
- species
- livestock type
- age
- weight
- health status
- current GPS position
- health event history
- weight history

#### Culture

Stores:

- crop name
- crop family
- planting date
- expected harvest date
- current growth stage
- soil requirements

#### PositionGPS

Stores latitude and longitude and can compute distance to another position using the haversine formula.

This is used by the GPS sensor logic.

## 5. Project Flow From Zero

This is the actual user journey.

### Step 1: Start the application

The launcher calls `ferme.Main.main(args)`.

### Step 2: Create a farm

The user selects option 1 in the main menu.

The app creates a new `Ferme` object with the farm name.

Nothing else exists yet. No zones, no sensors, no readings.

### Step 3: Add or import zones

The user can add zones manually or load them from a file.

Supported zone types:

- culture
- livestock
- aquaculture

When a zone is created, it becomes part of `Ferme.zones`.

### Step 4: Add crops or animals

After the zone exists, the user can attach domain entities to it.

- `ZoneCulture` receives `Culture`
- `ZoneElevage` receives `Animal`
- `ZoneAquacole` receives `EspeceAquacole`

The farm validates the zone type before attaching data.

### Step 5: Add sensors

Sensors are added per zone.

The farm stores them in `Ferme.capteurs`.

If the zone already exists, the sensor is also attached to that zone.

That association matters later for suspension, dashboards, and reading registration.

### Step 6: Register readings

The user can register readings manually or import them from files.

When a reading is submitted:

1. The app finds the sensor by code.
2. It checks whether the sensor zone exists.
3. It rejects the reading if the zone is suspended.
4. The sensor evaluates severity.
5. The reading is stored in the sensor history.
6. If the reading is GPS, the linked animal position is updated.
7. If severity is not `NORMAL`, an alert is generated.

This is the central runtime workflow of the application.

### Step 7: Review alerts and dashboards

The user can:

- view all alerts
- view active alerts only
- sort alerts by severity
- acknowledge an alert
- filter alert history by zone or severity
- inspect a zone dashboard
- inspect zone details

### Step 8: Suspend or reactivate a zone

Suspending a zone also suspends its associated sensors.

Reactivating a zone reactivates its sensors unless they are defective.

This is important because suspended zones do not accept readings.

## 6. Menu Structure

The main console menu in [ferme/Main.java](ferme/Main.java) is organized into the following areas:

- create or change farm
- zones
- crops
- animals
- sensors
- readings and production
- alerts
- dashboards
- zone suspension/activation
- load global data from file

Each submenu is a loop that keeps asking until the user chooses to go back.

This means the application is stateful and interactive, but still entirely local.

## 7. Import Files And Formats

The project supports batch loading from text files.

### 7.1 Global data import

Handled by [ferme/ImporteurDonnees.java](ferme/ImporteurDonnees.java).

This file can create:

- farm name
- zones
- crops
- animals
- sensors

The format is semicolon-separated.

Examples:

```text
FERME;Ferme El-Djazaïr
ZONE_CULTURE;ZC01;Champ Nord - Céréales
ZONE_ELEVAGE;ZE01;Pâturage Est - Bovins;RUMINANT;36.75;2.85;500
CULTURE;ZC01;Blé dur;CEREALE;2025-10-15;2026-06-20;6.0;7.5;40;60
ANIMAL;ZE01;1;Vache;RUMINANT;5;650.0;SAIN
CAPTEUR_ENV;ENV01;ZC01;TEMPERATURE;10;35
```

### 7.2 Reading imports

The farm can load numeric readings and GPS readings from separate files.

Numeric readings format:

```text
NUM;ENV01;22.4;degC;2026-04-28-08h
```

GPS readings format:

```text
GPS;GPS01;36.751;2.851;2026-04-28-10h
```

### 7.3 Validation During Import

The importer and validators reject bad data such as:

- malformed dates
- reversed min/max thresholds
- unknown enum values
- incomplete rows
- duplicate zone or sensor codes
- incompatible zone/entity types

## 8. Validation Rules

[ferme/ValidationUtils.java](ferme/ValidationUtils.java) centralizes the most important checks.

### Date validation

There are two date formats:

- `AAAA-MM-JJ` for production and crop dates
- `AAAA-MM-JJ-HHh` for readings

Invalid dates trigger `IllegalArgumentException`.

### Range validation

For thresholds, the code checks that max is greater than or equal to min.

This is used when creating sensors and when entering soil requirement ranges.

### Input validation in the console

The console layer keeps looping until valid input is entered.

That means:

- integers are re-requested if parsing fails
- decimals are re-requested if parsing fails
- enums are selected from a numbered list
- required strings cannot be empty

## 9. Polymorphism And Why The Design Works

This project is a good example of object-oriented polymorphism.

### 9.1 Zone polymorphism

`Ferme` stores all zones in `Map<String, Zone>`.

That means the farm can handle different zone types through the same API.

At runtime, the program checks the concrete subtype when it needs subtype-specific behavior.

### 9.2 Sensor polymorphism

`Ferme` stores all sensors in `Map<String, Capteur>`.

Each sensor subtype provides its own `getTypeCapteur()`, `getUnite()`, and in the case of GPS, its own reading evaluation logic.

### 9.3 Reading polymorphism

The farm stores readings as `Releve`, but the concrete reading subtype decides whether it is numeric or GPS.

That allows the same workflow to accept both data shapes.

## 10. What Happens When A Reading Arrives

This is the most important internal flow.

1. The user selects the reading menu.
2. The app creates either `ReleveNumerique` or `ReleveGPS`.
3. `Ferme.enregistrerReleve(...)` looks up the sensor.
4. It verifies the sensor zone exists.
5. It rejects readings if the zone is suspended.
6. It passes the reading to the sensor.
7. The sensor computes severity.
8. The reading is appended to sensor history.
9. If the reading is GPS, the animal position can be updated.
10. If severity is not normal, `Ferme` creates an alert.

That is the main automatic business rule chain of the application.

## 11. What Happens When A Zone Is Suspended

Suspension is not just a visual state.

When `Ferme.suspendreZone(codeZone)` is called:

- the zone status becomes suspended
- every sensor attached to that zone is also suspended
- suspended sensors stop accepting new readings

When the zone is reactivated:

- the zone status becomes active
- attached sensors are reactivated
- defective sensors remain defective in behavior, even if they are re-enabled through the zone lifecycle

## 12. Dashboards And Reports

The application exposes reporting views rather than exporting files.

### Zone dashboard

Shows:

- all sensors in a zone
- their status
- the latest reading for each sensor
- a color-coded severity summary in the terminal

### Zone detail view

Each zone subtype can print its own detailed state:

- cultures and their stages
- animals and their health
- species and feeding program
- production history

### Other reports

The app can also print:

- culture reports by zone
- culture filters by family
- animals by health status
- production by date range
- alert history filters

## 13. Code Organization Summary

### Root launcher

- [Main.java](Main.java)

### Application orchestration

- [ferme/Main.java](ferme/Main.java)

### Domain services

- [ferme/Ferme.java](ferme/Ferme.java)
- [ferme/ImporteurDonnees.java](ferme/ImporteurDonnees.java)
- [ferme/ValidationUtils.java](ferme/ValidationUtils.java)

### Models

- [ferme/models](ferme/models)

### Enums

- [ferme/enums](ferme/enums)

## 14. Mental Model For Reading The Code

If you want to understand the project quickly, read it in this order:

1. [Main.java](Main.java)
2. [ferme/Main.java](ferme/Main.java)
3. [ferme/Ferme.java](ferme/Ferme.java)
4. [ferme/ImporteurDonnees.java](ferme/ImporteurDonnees.java)
5. [ferme/models/Zone.java](ferme/models/Zone.java)
6. [ferme/models/Capteur.java](ferme/models/Capteur.java)
7. [ferme/models/Releve.java](ferme/models/Releve.java)
8. [ferme/models/Animal.java](ferme/models/Animal.java)
9. [ferme/models/Culture.java](ferme/models/Culture.java)
10. [ferme/models/Alerte.java](ferme/models/Alerte.java)

That gives you the full flow from entry point to domain behavior.

## 15. Example End-To-End Scenario

Here is one realistic path through the whole system.

1. Create a farm named `Ferme Test`.
2. Import `donnees/donnees_globales.txt`.
3. The import creates zones, crops, animals, and sensors.
4. Load readings from `donnees/releves_numeriques.txt` and `donnees/releves_gps.txt`.
5. Numeric readings are evaluated against thresholds.
6. GPS readings are evaluated against the zone radius.
7. Non-normal readings generate alerts.
8. Open the alerts menu and inspect active alerts.
9. Open a dashboard for `ZC01`.
10. The dashboard prints sensors, latest readings, and severity.

That is the complete operational lifecycle of the application.

## 16. What To Remember

This project is not just a set of menus. It is a small domain engine for a smart farm.

The important ideas are:

- `Ferme` stores the farm state
- zones own their local sub-entities
- sensors evaluate readings
- readings create alerts when abnormal
- GPS readings can move animals
- imports bootstrap the whole model from text files
- the console layer is only the interface

If you understand those seven ideas, you understand the entire project.

## 17. Exhaustive `Ferme` Public API

This section maps the real behavior exposed by [ferme/Ferme.java](ferme/Ferme.java). The class is the central service object and the entire console app is a user-facing wrapper around these methods.

### Farm identity and lifecycle

- `Ferme(String nomFerme)` creates a fresh farm with empty zone, sensor, and alert collections.
- `getNomFerme()` returns the farm name.

### Zone management

- `ajouterZone(Zone zone)` rejects duplicate zone codes, stores the zone in `zones`, and prints the created zone.
- `modifierZone(String codeZone, String nouveauNom)` renames an existing zone only.
- `activerZone(String codeZone)` activates the zone and also reactivates its attached sensors, except sensors that are logically defective and already marked as such by domain behavior.
- `suspendreZone(String codeZone)` suspends the zone and all associated sensors.
- `rechercherZone(String codeZone)` returns the zone from the zone map.
- `getZoneById(String id)` is a compatibility alias for `rechercherZone`.
- `afficherVueEnsembleZones()` prints every zone with numbering.

### Crop management

- `affecterCultureAZone(String codeZone, Culture culture)` only works on `ZoneCulture` instances.
- `enregistrerCulture(Culture culture)` scans the farm and adds the crop to the first culture zone found.
- `mettreAJourStade(String codeZone, int indexCulture, StadeCroissance nouveauStade)` updates the stage of a culture by index.
- `afficherStadeCroissance(String codeZone)` lists the stage of every culture in the zone.
- `genererRapportCulturesParZone(String codeZone)` produces a family and growth-stage report.
- `afficherCulturesParFamille(String codeZone, FamilleCulture famille)` filters crops across all zones or a single zone if a code is provided.

### Animal management

- `affecterAnimalAZone(String codeZone, Animal animal)` only works on `ZoneElevage` and rejects duplicate animal numbers globally.
- `enregistrerAnimal(Animal animal)` adds an animal to the first compatible livestock zone.
- `mettreAJourSante(int numeroAnimal, EtatSante nouvelEtat)` changes an animal health state.
- `enregistrerEvenementSanitaire(int numeroAnimal, String description, String date)` appends a health event.
- `enregistrerEvolutionPoids(int numeroAnimal, double nouveauPoids, String date)` stores a weight history entry and updates the current weight.
- `afficherAnimauxParEtatSante(String codeZone, EtatSante etat)` filters animals by health status, optionally scoped to one zone.
- `afficherProgrammeAlimentation(String codeZone)` prints the feeding plan for a livestock or aquaculture zone.

### Sensor management

- `ajouterCapteur(Capteur capteur)` rejects duplicate sensor codes, stores the sensor, and links it to the target zone if present.
- `configurerCapteur(String codeCapteur, double seuilMin, double seuilMax)` updates thresholds on an existing sensor.
- `changerStatutCapteur(String codeCapteur, StatutCapteur nouveauStatut)` sets the sensor status.
- `chargerCapteursDepuisFichier(String cheminFichier)` parses sensor rows from a file.
- `chargerCapteursDepuisFichiers(String[] cheminsFichiers)` iterates over multiple sensor files and sums accepted rows.

### Reading management

- `enregistrerReleve(String codeCapteur, Releve releve)` is the core runtime path for incoming data.
- `chargerRelevesDepuisFichier(String cheminFichier)` parses numeric or GPS reading rows from a file.
- `chargerRelevesDepuisFichiers(String[] cheminsFichiers)` loads multiple reading files.
- `afficherTableauDeBord(String codeZone)` prints the sensor dashboard for one zone.
- `consulterHistoriqueReleves(String codeCapteur, String dateDebut, String dateFin)` filters a sensor history by date.

### Alert management

- `afficherPanneauAlertes()` prints all alerts.
- `afficherAlertesActives()` prints only non-acknowledged alerts.
- `acquitterAlerte(int idAlerte)` marks an alert as acknowledged.
- `supprimerAlerte(int idAlerte)` removes an alert from the list.
- `consulterHistoriqueAlertes(String codeZone, NiveauGravite niveau, String dateDebut, String dateFin, Boolean acquittee)` applies a multi-criteria alert filter.
- `trierAlertesParGravite()` sorts alerts by severity.
- `afficherAlertesTrierParGravite()` prints the sorted alert list.

### Production management

- `enregistrerProduction(String codeZone, double valeur, String date)` writes a production entry to a zone.
- `afficherProductionParPlageDate(String codeZone, String dateDebut, String dateFin)` filters production entries in a zone.

## 18. Exhaustive Enum Inventory

These enums define the legal values that the console menus and importers accept.

### `EtatSante`

- `SAIN` -> Sain
- `MALADE` -> Malade
- `QUARANTAINE` -> Quarantaine

Used by animals and health-related filters.

### `FamilleCulture`

- `CEREALE` -> Céréale
- `LEGUME` -> Légume
- `FRUIT` -> Fruit

Used by crops and crop reporting.

### `NiveauGravite`

- `NORMAL` -> Normal
- `AVERTISSEMENT` -> Avertissement
- `CRITIQUE` -> Critique

Used by readings and alerts.

### `StadeCroissance`

- `SEMIS` -> Semis
- `GERMINATION` -> Germination
- `CROISSANCE` -> Croissance
- `MATURITE` -> Maturité
- `RECOLTE` -> Récolte

Used by crops and crop progression reports.

### `StatutCapteur`

- `ACTIF` -> Actif
- `DEFAILLANT` -> Défaillant
- `SUSPENDU` -> Suspendu

Used to enable, disable, and mark failure state for sensors.

### `StatutZone`

- `ACTIVE` -> Active
- `SUSPENDUE` -> Suspendue

Used to determine whether a zone can accept readings.

### `TypeElevage`

- `RUMINANT` -> Ruminant
- `VOLAILLE` -> Volaille

Used by livestock zones and animals.

### `TypeCapteurEnv`

- `TEMPERATURE` -> Température, unit `°C`
- `HUMIDITE` -> Humidité, unit `%`
- `PLUVIOMETRIE` -> Pluviométrie, unit `mm`

### `TypeCapteurSol`

- `PH` -> pH, unit empty string
- `HUMIDITE_SOL` -> Humidité du sol, unit `%`
- `AZOTE` -> Teneur en azote, unit `mg/kg`

### `TypeCapteurBio`

- `TEMPERATURE_CORPORELLE` -> Température corporelle, unit `°C`
- `ACTIVITE` -> Niveau d'activité, unit `pas/min`

### `TypeCapteurEau`

- `TEMPERATURE_EAU` -> Température eau, unit `°C`
- `OXYGENE_DISSOUS` -> Oxygène dissous, unit `mg/L`
- `PH_EAU` -> pH eau, unit empty string

## 19. Main Menu And Submenu Behavior In Detail

The console layer is strictly sequential and single-threaded. Every submenu is a `while` loop that exits only on the explicit return choice.

### Main menu

The root menu offers:

1. Create or change farm
2. Zones
3. Cultures
4. Animals
5. Sensors
6. Readings and production
7. Alerts
8. Dashboards
9. Suspension / activation
10. Load global data from file
0. Quit

Important behavior:

- Every submenu except the farm creation flow calls `requireFerme()` first.
- If the farm does not exist, the app throws `IllegalStateException`, which the menu loop catches and prints.
- Menu input is parsed as integers, so non-numeric values do not crash the app; the prompt repeats.

### Zones submenu

This is where the user builds the physical structure of the farm.

- View all zones
- Add culture zone
- Add livestock zone
- Add aquaculture zone
- Add aquatic species
- Define feeding program
- Record production

The menu directly maps to `Ferme` methods or subtype-specific methods on `ZoneAquacole` and `ZoneElevage`.

### Cultures submenu

This menu manages crop assignment and progression.

- Assign a crop to a zone
- Update crop growth stage
- Show stages for a zone
- Generate crop report by zone
- Filter crops by family

### Animals submenu

This menu manages livestock health and telemetry.

- Assign an animal to a zone
- Update health state
- Record a health event
- Record a weight evolution
- Show feeding program
- Filter by health state

### Sensors submenu

Sensors can be created manually or loaded from file.

- Add environmental sensor
- Add soil sensor
- Add biometric sensor
- Add GPS sensor
- Add water sensor
- Load sensors from file

### Readings and production submenu

- Record a numeric reading manually
- Load readings from files
- Query production by date range

### Alerts submenu

- Show all alerts
- Show only active alerts
- Sort alerts by severity
- Acknowledge an alert
- Filter alert history by zone
- Filter alert history by severity

### Dashboards submenu

- Zone dashboard by zone code
- Zone details by selected zone object

### Suspension submenu

- Suspend a zone
- Activate a zone

### Data import option

This option invokes the global importer and is only available once a farm exists.

## 20. File Format Contracts

### 20.1 Global data file format

Handled by [ferme/ImporteurDonnees.java](ferme/ImporteurDonnees.java).

Supported line types and field order:

| Type | Fields |
|---|---|
| `FERME` | `FERME;nom` |
| `ZONE_CULTURE` | `ZONE_CULTURE;code;nom` |
| `ZONE_ELEVAGE` | `ZONE_ELEVAGE;code;nom;TypeElevage;latitude;longitude;surface` |
| `ZONE_AQUACOLE` | `ZONE_AQUACOLE;code;nom` |
| `CULTURE` | `CULTURE;codeZone;nom;FamilleCulture;datePlantation;dateRecolte;phMin;phMax;humMin;humMax` |
| `ANIMAL` | `ANIMAL;codeZone;numero;espece;TypeElevage;age;poids;EtatSante` |
| `CAPTEUR_ENV` | `CAPTEUR_ENV;code;codeZone;TypeCapteurEnv;seuilMin;seuilMax` |
| `CAPTEUR_SOL` | `CAPTEUR_SOL;code;codeZone;TypeCapteurSol;seuilMin;seuilMax` |
| `CAPTEUR_BIO` | `CAPTEUR_BIO;code;codeZone;TypeCapteurBio;seuilMin;seuilMax;numeroAnimal` |
| `CAPTEUR_GPS` | `CAPTEUR_GPS;code;codeZone;numeroAnimal;latitude;longitude;rayon` |
| `CAPTEUR_EAU` | `CAPTEUR_EAU;code;codeZone;TypeCapteurEau;seuilMin;seuilMax` |

Important contract details:

- All parsing is semicolon-based.
- The importer accepts comment lines starting with `#`.
- Blank lines are ignored.
- Enum values are matched with `valueOf` after `toUpperCase()`, so exact enum names are required.
- If a line is malformed, the importer prints the error and continues.

### 20.2 Sensor file loader format in `Ferme`

This loader uses a different field order from the global importer. That difference matters.

| Type | Fields |
|---|---|
| `ENV` or `ENVIRONNEMENTAL` | `type;code;codeZone;seuilMin;seuilMax;TypeCapteurEnv` |
| `SOL` | `type;code;codeZone;seuilMin;seuilMax;TypeCapteurSol` |
| `BIO` | `type;code;codeZone;seuilMin;seuilMax;TypeCapteurBio;numeroAnimal` |
| `GPS` | `type;code;codeZone;numeroAnimal;latitude;longitude;rayon` |
| `EAU` | `type;code;codeZone;seuilMin;seuilMax;TypeCapteurEau` |

### 20.3 Reading file formats

Numeric readings:

```text
NUM;codeCapteur;valeur;unite;date
```

GPS readings:

```text
GPS;codeCapteur;latitude;longitude;date
```

Dates for readings must use the `AAAA-MM-JJ-HHh` format.

### 20.4 Production and crop date format

The project uses plain ISO dates:

```text
AAAA-MM-JJ
```

This is enforced by `ValidationUtils.validerDateSimple`.

## 21. Validation And Business Rules

### Date parsing

`ValidationUtils.validerDateSimple`:

- rejects `null`
- trims whitespace
- parses using ISO local date
- throws a clear message if the date is invalid

`ValidationUtils.validerDateReleve`:

- rejects `null`
- trims whitespace
- requires the regex `^\d{4}-\d{2}-\d{2}-\d{2}h$`
- parses the date part and hour part separately
- enforces hour between 00 and 23

### Threshold validation

`ValidationUtils.validerBornes` ensures `max >= min`.

This is used for:

- sensor thresholds
- crop soil requirements

### Sensor severity evaluation

For numeric sensors, `Capteur.evaluerNiveau` works like this:

- value in range -> `NORMAL`
- value outside range -> `AVERTISSEMENT`
- value far enough outside the range, where the relative deviation is greater than 0.5 of the threshold span -> `CRITIQUE`

The severity is therefore proportional to how far the value is beyond the configured range.

### GPS severity evaluation

For GPS sensors:

- inside radius -> `NORMAL`
- beyond radius -> `AVERTISSEMENT`
- beyond 1.5 times radius -> `CRITIQUE`

The code uses the haversine distance computed by [PositionGPS](ferme/models/PositionGPS.java).

### Alert generation

An alert is created only when the recorded reading is accepted and the severity is not `NORMAL`.

If the zone is suspended, the reading is rejected before alert generation.

### Zone and entity compatibility

- Crops only belong in `ZoneCulture`.
- Animals only belong in `ZoneElevage`.
- Aquatic species only belong in `ZoneAquacole`.
- Sensor addition attaches by zone code, but the farm does not block creation if the zone is missing at creation time; the sensor just remains globally stored and unlinked until a matching zone exists.

## 22. Internal Algorithms And Implementation Details

### 22.1 GPS distance calculation

[PositionGPS.distanceVers](ferme/models/PositionGPS.java) uses the haversine formula.

The Earth radius is hardcoded as 6,371,000 meters. That means the distance computation is approximate but suitable for the domain.

### 22.2 Alert ordering

`Ferme.trierAlertesParGravite()` sorts by the ordinal order of `NiveauGravite`.

Because the enum declaration is `NORMAL`, `AVERTISSEMENT`, `CRITIQUE`, the sort order follows that exact declaration order.

### 22.3 Sensor dashboard rendering

`Ferme.afficherTableauDeBord` iterates over every sensor and selects only those whose `codeZone` matches the requested zone.

For each sensor, it prints:

- a status marker
- the sensor summary line
- the most recent reading if one exists

### 22.4 Production history storage

`HistoriqueProduction` uses two parallel lists:

- dates
- values

The unit is stored once per history instance.

This is simple and compact but means the entry data depends on keeping the two lists in sync.

### 22.5 Animal health and weight history

`Animal` stores event and weight histories in parallel lists as well.

That gives the class a simple append-only audit trail for:

- sanitary events
- weight evolution

### 22.6 Zone activation cascade

When a zone activates, it iterates over all attached sensors and reactivates those that are not defective.

When it suspends, it suspends all attached sensors.

That means zone state is the controlling state for most sensor behavior.

## 23. Known Quirks, Limitations, And Practical Observations

This section is important because it explains the actual behavior of the code, not just the intended model.

### 23.1 No persistence layer

Everything is in-memory only.

If the program exits, the farm state is lost unless it is reloaded from files.

### 23.2 The program is terminal-only

There is no GUI, web server, or API.

### 23.3 Some menus depend on object creation order

You must create the farm before any submenu works.

You must create zones before attaching zone-scoped data.

You must create sensors before readings can be accepted.

### 23.4 Sensor and importer file formats are not identical

This is a real implementation detail and a common source of confusion.

- global import uses `TYPE;CODE;...`
- sensor file loading inside `Ferme` uses a different order for the first fields

### 23.5 `ProgrammeAlimentation` stores less than its constructor suggests

The constructor accepts `nombreRepasParJour`, but the current class body does not persist that value.

This means the feeding program is partially modeled and partially incomplete.

### 23.6 `EspeceAquacole` equality is reference-based

The aquaculture zone uses `List.contains` before adding a species.

Since `EspeceAquacole` does not override `equals`, duplicate detection depends on object identity rather than species name and count.

### 23.7 Alerts have a hard cap

`Ferme.MAX_ALERTES` is 500.

Once that cap is reached, the farm prints a warning and stops adding new alerts.

### 23.8 Sensor status and zone status interact

Suspending a zone suspends its sensors.

Reactivating a zone reactivates sensors except those that are defective according to sensor-level state.

### 23.9 GPS sensor logic also updates an animal

When a GPS reading is accepted, the linked animal position is updated automatically if the animal exists.

If the animal does not exist, the code prints a warning and continues.

### 23.10 Date filtering is string comparison

Production, reading, and alert date filters use lexicographic string comparison on normalized date strings.

That works because the dates are stored in sortable `YYYY-MM-DD` or `YYYY-MM-DD-HHh` format.

### 23.11 Empty interfaces in the source tree are not used by the runtime flow

The current workspace tree shows an `interfaces` folder, but the runtime flow is driven by the concrete `Main`, `Ferme`, and model classes.

### 23.12 Some validation is structural rather than semantic

For example, the code validates date syntax and threshold ordering, but it does not deeply validate domain plausibility such as whether a crop family matches a particular zone climate.

## 24. Concrete Runtime Examples

### 24.1 Creating a farm and importing data

1. The user selects farm creation.
2. `new Ferme(name)` initializes empty maps and lists.
3. The user imports `donnees/donnees_globales.txt`.
4. Zones are inserted first.
5. Crops and animals attach to the already-created zones.
6. Sensors are stored and linked by zone code.

### 24.2 Loading readings and generating alerts

1. The user loads a numeric reading file.
2. Each row becomes `ReleveNumerique`.
3. Each reading is matched with a sensor by code.
4. The sensor calculates severity.
5. Abnormal readings create alerts.
6. GPS readings also attempt to update the linked animal.

### 24.3 Suspending a zone

1. The user selects the suspension menu.
2. The zone status changes to suspended.
3. All zone sensors are suspended.
4. Future readings for those sensors are ignored until reactivation.

### 24.4 Dashboard inspection

1. The user chooses a zone code.
2. The farm scans all sensors.
3. It prints only the sensors belonging to that zone.
4. It displays each sensor status and its latest reading.

## 25. Practical Read-Order For The Codebase

If you want to understand the project in the fastest possible way, read it in this order:

1. [Main.java](Main.java)
2. [ferme/Main.java](ferme/Main.java)
3. [ferme/ValidationUtils.java](ferme/ValidationUtils.java)
4. [ferme/models/Releve.java](ferme/models/Releve.java)
5. [ferme/models/Capteur.java](ferme/models/Capteur.java)
6. [ferme/models/Zone.java](ferme/models/Zone.java)
7. [ferme/Ferme.java](ferme/Ferme.java)
8. [ferme/ImporteurDonnees.java](ferme/ImporteurDonnees.java)
9. All model subclasses under [ferme/models](ferme/models)

That order follows the runtime path from startup to domain behavior.

## 26. Final Technical Summary

This project is a menu-driven object-oriented farm management simulator.

The console layer parses user input and delegates to the domain layer. The domain layer owns the farm state, evaluates readings, generates alerts, updates animal positions, and prints dashboards. The importer layer bootstraps the entire model from structured text files. Validation is centralized and consistent. The whole system is in-memory, deterministic, and designed around clear ownership boundaries between farm, zones, sensors, readings, and alerts.

If you understand the farm as the root aggregate, the zones as the structural containers, the sensors as evaluators, and the readings as the trigger for alerts and movement, you understand the whole application.
