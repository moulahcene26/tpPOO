package ferme.ui.services;

import ferme.Ferme;
import ferme.ImporteurDonnees;
import ferme.ValidationUtils;
import ferme.enums.EtatSante;
import ferme.enums.FamilleCulture;
import ferme.enums.NiveauGravite;
import ferme.enums.StatutCapteur;
import ferme.enums.StatutZone;
import ferme.enums.StadeCroissance;
import ferme.enums.TypeCapteurBio;
import ferme.enums.TypeCapteurEau;
import ferme.enums.TypeCapteurEnv;
import ferme.enums.TypeCapteurSol;
import ferme.enums.TypeElevage;
import ferme.models.Alerte;
import ferme.models.Animal;
import ferme.models.Capteur;
import ferme.models.CapteurBiometrique;
import ferme.models.CapteurEau;
import ferme.models.CapteurEnvironnemental;
import ferme.models.CapteurGPS;
import ferme.models.CapteurSol;
import ferme.models.Culture;
import ferme.models.EspeceAquacole;
import ferme.models.HistoriqueProduction;
import ferme.models.ExigencesPedologiques;
import ferme.models.PositionGPS;
import ferme.models.ProgrammeAlimentation;
import ferme.models.ReleveGPS;
import ferme.models.ReleveNumerique;
import ferme.models.Zone;
import ferme.models.ZoneAquacole;
import ferme.models.ZoneCulture;
import ferme.models.ZoneElevage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import ferme.ui.AppContext;

public class UiFarmService {
    private final AppContext context;

    public UiFarmService(AppContext context) {
        this.context = context;
    }

    public boolean createFarm(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Farm name is required.");
        }
        context.setFerme(new Ferme(name.trim()));
        return true;
    }

    public boolean hasFarm() {
        return context.hasFerme();
    }

    public String getCurrentFarmName() {
        if (!context.hasFerme()) {
            return "No farm";
        }
        return context.getFerme().getNomFerme();
    }

    public List<Zone> getZones() {
        if (!context.hasFerme()) {
            return Collections.emptyList();
        }
        return context.getFerme().getZones();
    }

    public List<Capteur> getSensors() {
        if (!context.hasFerme()) {
            return Collections.emptyList();
        }
        return context.getFerme().getCapteurs();
    }

    public List<Alerte> getAlerts() {
        if (!context.hasFerme()) {
            return Collections.emptyList();
        }
        return context.getFerme().getAlertes();
    }

    public int getAlertCount() {
        return getAlerts().size();
    }

    public int getActiveAlertCount() {
        int count = 0;
        for (Alerte a : getAlerts()) {
            if (!a.estAcquittee()) {
                count++;
            }
        }
        return count;
    }

    public int getCriticalAlertCount() {
        int count = 0;
        for (Alerte a : getAlerts()) {
            if (a.getNiveau() == NiveauGravite.CRITIQUE) {
                count++;
            }
        }
        return count;
    }

    public int getZoneCount() {
        return getZones().size();
    }

    public int getActiveZoneCount() {
        int count = 0;
        for (Zone zone : getZones()) {
            if (zone.getStatut() == StatutZone.ACTIVE) {
                count++;
            }
        }
        return count;
    }

    public int getSuspendedZoneCount() {
        int count = 0;
        for (Zone zone : getZones()) {
            if (zone.getStatut() == StatutZone.SUSPENDUE) {
                count++;
            }
        }
        return count;
    }

    public int getSensorCount() {
        return getSensors().size();
    }

    public int getActiveSensorCount() {
        int count = 0;
        for (Capteur capteur : getSensors()) {
            if (capteur.getStatut() == StatutCapteur.ACTIF) {
                count++;
            }
        }
        return count;
    }

    public int getDefectiveSensorCount() {
        int count = 0;
        for (Capteur capteur : getSensors()) {
            if (capteur.getStatut() == StatutCapteur.DEFAILLANT) {
                count++;
            }
        }
        return count;
    }

    public int getAnimalCount() {
        int count = 0;
        for (Zone zone : getZones()) {
            if (zone instanceof ZoneElevage) {
                count += ((ZoneElevage) zone).getAnimaux().size();
            }
            if (zone instanceof ZoneAquacole) {
                count += ((ZoneAquacole) zone).getNombreTotalAnimaux();
            }
        }
        return count;
    }

    public int getCropCount() {
        int count = 0;
        for (Zone zone : getZones()) {
            if (zone instanceof ZoneCulture) {
                count += ((ZoneCulture) zone).getCultures().size();
            }
        }
        return count;
    }

    public boolean addCultureZone(String code, String name) {
        Ferme ferme = context.requireFerme();
        return ferme.ajouterZone(new ZoneCulture(code, name));
    }

    public boolean addLivestockZone(String code, String name, TypeElevage type,
            double latitude, double longitude, double radius) {
        Ferme ferme = context.requireFerme();
        return ferme.ajouterZone(new ZoneElevage(code, name, type, new PositionGPS(latitude, longitude), radius));
    }

    public boolean addAquacultureZone(String code, String name) {
        Ferme ferme = context.requireFerme();
        return ferme.ajouterZone(new ZoneAquacole(code, name));
    }

    public boolean addAquacultureSpecies(String zoneCode, String speciesName, int quantity) {
        Ferme ferme = context.requireFerme();
        Zone zone = ferme.rechercherZone(zoneCode);
        if (!(zone instanceof ZoneAquacole)) {
            throw new IllegalArgumentException("Zone not found or not aquaculture: " + zoneCode);
        }
        return ((ZoneAquacole) zone).ajouterEspece(new EspeceAquacole(speciesName, quantity));
    }

    public boolean renameZone(String code, String newName) {
        return context.requireFerme().modifierZone(code, newName);
    }

    public boolean suspendZone(String code) {
        return context.requireFerme().suspendreZone(code);
    }

    public boolean activateZone(String code) {
        return context.requireFerme().activerZone(code);
    }

    public boolean addCulture(String zoneCode, String name, FamilleCulture famille,
            String datePlantation, String dateRecolte,
            double phMin, double phMax, double humMin, double humMax) {
        ValidationUtils.validerBornes(phMin, phMax, "pH min", "pH max");
        ValidationUtils.validerBornes(humMin, humMax, "humidite min", "humidite max");
        Culture culture = new Culture(name, famille, datePlantation, dateRecolte,
                new ExigencesPedologiques(phMin, phMax, humMin, humMax));
        return context.requireFerme().affecterCultureAZone(zoneCode, culture);
    }

    public boolean updateCultureStage(String zoneCode, int index, StadeCroissance stage) {
        return context.requireFerme().mettreAJourStade(zoneCode, index, stage);
    }

    public boolean addAnimal(String zoneCode, int numero, String espece, TypeElevage type,
            int age, double poids, EtatSante etat) {
        Animal animal = new Animal(numero, espece, type, age, poids, etat);
        return context.requireFerme().affecterAnimalAZone(zoneCode, animal);
    }

    public boolean updateAnimalHealth(int numero, EtatSante etat) {
        return context.requireFerme().mettreAJourSante(numero, etat);
    }

    public boolean addAnimalEvent(int numero, String description, String date) {
        return context.requireFerme().enregistrerEvenementSanitaire(numero, description, date);
    }

    public boolean addAnimalWeight(int numero, double poids, String date) {
        return context.requireFerme().enregistrerEvolutionPoids(numero, poids, date);
    }

    public boolean setFeedingProgram(Zone zone, String aliment, double dose, int freq) {
        ProgrammeAlimentation programme = new ProgrammeAlimentation(aliment, dose, freq);
        if (zone instanceof ZoneElevage) {
            ((ZoneElevage) zone).setProgrammeAlimentation(programme);
            return true;
        }
        if (zone instanceof ZoneAquacole) {
            ((ZoneAquacole) zone).setProgrammeAlimentation(programme);
            return true;
        }
        return false;
    }

    public boolean updateFeedingProgram(String zoneCode, String aliment, double dose, int freq) {
        Zone zone = context.requireFerme().rechercherZone(zoneCode);
        if (zone == null) {
            throw new IllegalArgumentException("Zone not found: " + zoneCode);
        }
        return setFeedingProgram(zone, aliment, dose, freq);
    }

    public boolean recordHarvestWeight(String zoneCode, double weight, String date) {
        context.requireFerme().enregistrerProduction(zoneCode, weight, date);
        return true;
    }

    public List<ZoneAquacole> getAquacultureZones() {
        if (!context.hasFerme()) {
            return Collections.emptyList();
        }
        List<ZoneAquacole> zones = new ArrayList<>();
        for (Zone zone : context.getFerme().getZones()) {
            if (zone instanceof ZoneAquacole) {
                zones.add((ZoneAquacole) zone);
            }
        }
        return zones;
    }

    public List<Zone> getFeedingProgramZones() {
        if (!context.hasFerme()) {
            return Collections.emptyList();
        }
        List<Zone> zones = new ArrayList<>();
        for (Zone zone : context.getFerme().getZones()) {
            ProgrammeAlimentation programme = getFeedingProgram(zone);
            if (programme != null) {
                zones.add(zone);
            }
        }
        return zones;
    }

    public List<Zone> getFeedableZones() {
        if (!context.hasFerme()) {
            return Collections.emptyList();
        }
        List<Zone> zones = new ArrayList<>();
        for (Zone zone : context.getFerme().getZones()) {
            if (zone instanceof ZoneElevage || zone instanceof ZoneAquacole) {
                zones.add(zone);
            }
        }
        return zones;
    }

    public ProgrammeAlimentation getFeedingProgram(Zone zone) {
        if (zone instanceof ZoneElevage) {
            return ((ZoneElevage) zone).getProgrammeAlimentation();
        }
        if (zone instanceof ZoneAquacole) {
            return ((ZoneAquacole) zone).getProgrammeAlimentation();
        }
        return null;
    }

    public boolean addEnvSensor(String code, String zone, TypeCapteurEnv type, double min, double max) {
        return context.requireFerme().ajouterCapteur(new CapteurEnvironnemental(code, zone, min, max, type));
    }

    public boolean addSoilSensor(String code, String zone, TypeCapteurSol type, double min, double max) {
        return context.requireFerme().ajouterCapteur(new CapteurSol(code, zone, min, max, type));
    }

    public boolean addBioSensor(String code, String zone, TypeCapteurBio type, double min, double max, int animal) {
        return context.requireFerme().ajouterCapteur(new CapteurBiometrique(code, zone, min, max, type, animal));
    }

    public boolean addGpsSensor(String code, String zone, int animal, double lat, double lon, double radius) {
        return context.requireFerme().ajouterCapteur(new CapteurGPS(code, zone, animal, lat, lon, radius));
    }

    public boolean addWaterSensor(String code, String zone, TypeCapteurEau type, double min, double max) {
        return context.requireFerme().ajouterCapteur(new CapteurEau(code, zone, min, max, type));
    }

    public boolean configureSensor(String code, double min, double max) {
        return context.requireFerme().configurerCapteur(code, min, max);
    }

    public boolean changeSensorStatus(String code, StatutCapteur status) {
        return context.requireFerme().changerStatutCapteur(code, status);
    }

    public ReleveNumerique recordNumericReading(String sensorCode, double value, String unit, String date) {
        ReleveNumerique releve = new ReleveNumerique(value, unit, date, sensorCode);
        boolean ok = context.requireFerme().enregistrerReleve(sensorCode, releve);
        return ok ? releve : null;
    }

    public ReleveGPS recordGpsReading(String sensorCode, double lat, double lon, String date) {
        ReleveGPS releve = new ReleveGPS(new PositionGPS(lat, lon), date, sensorCode);
        boolean ok = context.requireFerme().enregistrerReleve(sensorCode, releve);
        return ok ? releve : null;
    }

    public void recordProduction(String zoneCode, double value, String date) {
        context.requireFerme().enregistrerProduction(zoneCode, value, date);
    }

    public List<String[]> getProductionEntries(String zoneCode, String startDate, String endDate) {
        List<String[]> entries = new ArrayList<>();
        Zone zone = context.requireFerme().rechercherZone(zoneCode);
        if (zone == null) return entries;
        HistoriqueProduction histo = zone.getHistoriqueProduction();
        for (int i = 0; i < histo.getNbEntrees(); i++) {
            String date = histo.getDate(i);
            if ((startDate == null || date.compareTo(startDate) >= 0)
                    && (endDate == null || date.compareTo(endDate) <= 0)) {
                entries.add(new String[]{date, String.valueOf(histo.getValeur(i)), histo.getUnite()});
            }
        }
        return entries;
    }

    public Animal findAnimal(int animalNumber) {
        for (Zone zone : context.requireFerme().getZones()) {
            if (zone instanceof ZoneElevage) {
                for (Animal a : ((ZoneElevage) zone).getAnimaux()) {
                    if (a.getNumero() == animalNumber) return a;
                }
            }
        }
        return null;
    }

    public int importGlobalData(File file) {
        return ImporteurDonnees.chargerDepuisFichier(context.requireFerme(), file.getPath());
    }

    public int importSensors(File file) {
        return context.requireFerme().chargerCapteursDepuisFichier(file.getPath());
    }

    public int importReadings(File file) {
        return context.requireFerme().chargerRelevesDepuisFichier(file.getPath());
    }

    public List<Alerte> getRecentAlerts(int limit) {
        List<Alerte> alerts = new ArrayList<>(getAlerts());
        alerts.sort(Comparator.comparing(Alerte::getDateCreation).reversed());
        if (alerts.size() <= limit) {
            return alerts;
        }
        return alerts.subList(0, limit);
    }

    public List<Alerte> filterAlerts(String zone, NiveauGravite niveau, Boolean active) {
        return getAlerts().stream()
                .filter(a -> zone == null || zone.isEmpty() || a.getCodeZone().equals(zone))
                .filter(a -> niveau == null || a.getNiveau() == niveau)
                .filter(a -> active == null || (active && !a.estAcquittee()) || (!active && a.estAcquittee()))
                .collect(Collectors.toList());
    }

    public boolean acknowledgeAlert(int id) {
        return context.requireFerme().acquitterAlerte(id);
    }

    public boolean deleteAlert(int id) {
        return context.requireFerme().supprimerAlerte(id);
    }

    public List<Alerte> getAlertHistory() {
        if (!context.hasFerme()) {
            return Collections.emptyList();
        }
        return context.getFerme().getAlertesHistorique();
    }

    public Ferme getFarm() {
        return context.getFerme();
    }

    public void clearFarm() {
        context.setFerme(null);
    }
}
