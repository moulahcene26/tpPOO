package ferme;

import ferme.enums.*;
import ferme.models.*;

public class Main {

    public static void main(String[] args) {

        System.out.println("============================================================");
        System.out.println("    APPLICATION DE GESTION INTELLIGENTE DE LA FERME");
        System.out.println("============================================================\n");

        // ====================== CRÉATION DE LA FERME ======================
        Ferme ferme = new Ferme("Ferme El-Djazaïr");

        // ====================== 1. GESTION DES ZONES ======================
        System.out.println(">>>>> 1. CRÉATION DES ZONES <<<<<\n");

        ZoneCulture zc1 = new ZoneCulture("ZC01", "Champ Nord - Céréales");
        ZoneCulture zc2 = new ZoneCulture("ZC02", "Parcelle Sud - Maraîchage");

        ZoneElevage ze1 = new ZoneElevage("ZE01", "Pâturage Est - Bovins",
                TypeElevage.RUMINANT, new PositionGPS(36.75, 2.85), 500);
        ZoneElevage ze2 = new ZoneElevage("ZE02", "Poulailler Central",
                TypeElevage.VOLAILLE, new PositionGPS(36.76, 2.86), 100);

        ZoneAquacole za1 = new ZoneAquacole("ZA01", "Bassin Aquacole");

        ferme.ajouterZone(zc1);
        ferme.ajouterZone(zc2);
        ferme.ajouterZone(ze1);
        ferme.ajouterZone(ze2);
        ferme.ajouterZone(za1);

        // Programme d'alimentation
        ze1.setProgrammeAlimentation(new ProgrammeAlimentation("Foin et granulés", 5.0, 3));
        ze2.setProgrammeAlimentation(new ProgrammeAlimentation("Grains de maïs", 0.15, 4));
        za1.setProgrammeAlimentation(new ProgrammeAlimentation("Granulés poissons", 0.5, 2));

        ferme.afficherVueEnsembleZones();

        // ====================== 2. GESTION DES CULTURES ======================
        System.out.println("\n>>>>> 2. ENREGISTREMENT DES CULTURES <<<<<\n");

        Culture ble = new Culture("Blé dur", FamilleCulture.CEREALE,
                "2025-10-15", "2026-06-20",
                new ExigencesPedologiques(6.0, 7.5, 40, 60));
        Culture mais = new Culture("Maïs", FamilleCulture.CEREALE,
                "2026-03-01", "2026-09-15",
                new ExigencesPedologiques(5.5, 7.0, 50, 70));
        Culture tomate = new Culture("Tomate", FamilleCulture.LEGUME,
                "2026-03-15", "2026-08-30",
                new ExigencesPedologiques(6.0, 6.8, 55, 75));
        Culture olive = new Culture("Olivier", FamilleCulture.FRUIT,
                "2025-01-01", "2025-11-15",
                new ExigencesPedologiques(6.5, 8.0, 30, 50));

        ferme.affecterCultureAZone("ZC01", ble);
        ferme.affecterCultureAZone("ZC01", mais);
        ferme.affecterCultureAZone("ZC02", tomate);
        ferme.affecterCultureAZone("ZC02", olive);

        // Mise à jour des stades
        ferme.mettreAJourStade("ZC01", 0, StadeCroissance.CROISSANCE);
        ferme.mettreAJourStade("ZC02", 0, StadeCroissance.GERMINATION);

        ferme.afficherStadeCroissance("ZC01");
        ferme.genererRapportCulturesParZone("ZC01");

        // ====================== 3. GESTION DES ANIMAUX ======================
        System.out.println("\n>>>>> 3. ENREGISTREMENT DES ANIMAUX <<<<<\n");

        Animal vache1 = new Animal(1, "Vache", TypeElevage.RUMINANT, 5, 650.0);
        vache1.setEtatSante(EtatSante.SAIN);
        Animal vache2 = new Animal(2, "Vache", TypeElevage.RUMINANT, 3, 580.0);
        vache2.setEtatSante(EtatSante.SAIN);
        Animal mouton1 = new Animal(3, "Mouton", TypeElevage.RUMINANT, 2, 45.0);
        mouton1.setEtatSante(EtatSante.SAIN);

        Animal poulet1 = new Animal(101, "Poulet", TypeElevage.VOLAILLE, 1, 2.5);
        poulet1.setEtatSante(EtatSante.SAIN);
        Animal dinde1 = new Animal(102, "Dinde", TypeElevage.VOLAILLE, 1, 8.0);
        dinde1.setEtatSante(EtatSante.SAIN);

        ferme.affecterAnimalAZone("ZE01", vache1);
        ferme.affecterAnimalAZone("ZE01", vache2);
        ferme.affecterAnimalAZone("ZE01", mouton1);
        ferme.affecterAnimalAZone("ZE02", poulet1);
        ferme.affecterAnimalAZone("ZE02", dinde1);

        // Espèces aquacoles
        za1.ajouterEspece(new EspeceAquacole("Tilapia", 200));
        za1.ajouterEspece(new EspeceAquacole("Crevette", 500));

        // Événements sanitaires
        ferme.mettreAJourSante(2, EtatSante.MALADE);
        ferme.enregistrerEvenementSanitaire(2, "Fièvre détectée, antibiotiques administrés", "2026-04-20");
        ferme.enregistrerEvolutionPoids(1, 655, "2026-04-15");
        ferme.enregistrerEvolutionPoids(1, 660, "2026-04-28");

        ferme.afficherProgrammeAlimentation("ZE01");

        // ====================== 4. GESTION DES CAPTEURS ======================
        System.out.println("\n>>>>> 4. AJOUT ET CONFIGURATION DES CAPTEURS <<<<<\n");

        // Capteurs environnementaux pour zone de culture
        CapteurEnvironnemental cEnvTemp = new CapteurEnvironnemental("ENV01", "ZC01", 10, 35, TypeCapteurEnv.TEMPERATURE);
        CapteurEnvironnemental cEnvHum = new CapteurEnvironnemental("ENV02", "ZC01", 40, 80, TypeCapteurEnv.HUMIDITE);
        CapteurEnvironnemental cEnvPluv = new CapteurEnvironnemental("ENV03", "ZC01", 0, 50, TypeCapteurEnv.PLUVIOMETRIE);

        // Capteurs de sol
        CapteurSol cSolPH = new CapteurSol("SOL01", "ZC01", 5.5, 7.5, TypeCapteurSol.PH);
        CapteurSol cSolHum = new CapteurSol("SOL02", "ZC01", 35, 65, TypeCapteurSol.HUMIDITE_SOL);
        CapteurSol cSolAzote = new CapteurSol("SOL03", "ZC01", 20, 80, TypeCapteurSol.AZOTE);

        // Capteurs biométriques pour animaux
        CapteurBiometrique cBioTemp1 = new CapteurBiometrique("BIO01", "ZE01", 37.5, 39.5,
                TypeCapteurBio.TEMPERATURE_CORPORELLE, 1);
        CapteurBiometrique cBioAct1 = new CapteurBiometrique("BIO02", "ZE01", 10, 80,
                TypeCapteurBio.ACTIVITE, 1);

        // Capteur GPS
        CapteurGPS cGPS1 = new CapteurGPS("GPS01", "ZE01", 1, 36.75, 2.85, 500);

        // Capteurs eau
        CapteurEau cEauTemp = new CapteurEau("EAU01", "ZA01", 18, 28, TypeCapteurEau.TEMPERATURE_EAU);
        CapteurEau cEauOxy = new CapteurEau("EAU02", "ZA01", 5, 12, TypeCapteurEau.OXYGENE_DISSOUS);
        CapteurEau cEauPH = new CapteurEau("EAU03", "ZA01", 6.5, 8.5, TypeCapteurEau.PH_EAU);

        ferme.ajouterCapteur(cEnvTemp);
        ferme.ajouterCapteur(cEnvHum);
        ferme.ajouterCapteur(cEnvPluv);
        ferme.ajouterCapteur(cSolPH);
        ferme.ajouterCapteur(cSolHum);
        ferme.ajouterCapteur(cSolAzote);
        ferme.ajouterCapteur(cBioTemp1);
        ferme.ajouterCapteur(cBioAct1);
        ferme.ajouterCapteur(cGPS1);
        ferme.ajouterCapteur(cEauTemp);
        ferme.ajouterCapteur(cEauOxy);
        ferme.ajouterCapteur(cEauPH);

        // ====================== ENVOI DE RELEVÉS ======================
        System.out.println("\n>>>>> SIMULATION D'ENVOI DE RELEVÉS <<<<<\n");

        // Relevés normaux
        ferme.enregistrerReleve("ENV01", new ReleveNumerique(22.5, "°C", "2026-04-28-08h", "ENV01"));
        ferme.enregistrerReleve("ENV01", new ReleveNumerique(25.0, "°C", "2026-04-28-12h", "ENV01"));
        ferme.enregistrerReleve("ENV01", new ReleveNumerique(28.3, "°C", "2026-04-28-16h", "ENV01"));

        // Relevé déclenche AVERTISSEMENT
        ferme.enregistrerReleve("ENV01", new ReleveNumerique(36.5, "°C", "2026-04-28-18h", "ENV01"));

        // Relevé déclenche CRITIQUE
        ferme.enregistrerReleve("ENV01", new ReleveNumerique(48.0, "°C", "2026-04-29-14h", "ENV01"));

        // Relevés sol
        ferme.enregistrerReleve("SOL01", new ReleveNumerique(6.8, "", "2026-04-28-09h", "SOL01"));
        ferme.enregistrerReleve("SOL01", new ReleveNumerique(4.5, "", "2026-04-29-09h", "SOL01"));

        // Relevés biométriques
        ferme.enregistrerReleve("BIO01", new ReleveNumerique(38.5, "°C", "2026-04-28-07h", "BIO01"));
        ferme.enregistrerReleve("BIO01", new ReleveNumerique(40.2, "°C", "2026-04-28-19h", "BIO01"));

        // Relevé GPS - animal dans la zone
        ferme.enregistrerReleve("GPS01", new ReleveGPS(new PositionGPS(36.751, 2.851), "2026-04-28-10h", "GPS01"));
        // Relevé GPS - animal HORS zone
        ferme.enregistrerReleve("GPS01", new ReleveGPS(new PositionGPS(36.80, 2.90), "2026-04-28-15h", "GPS01"));

        // Relevés eau
        ferme.enregistrerReleve("EAU01", new ReleveNumerique(24.0, "°C", "2026-04-28-08h", "EAU01"));
        ferme.enregistrerReleve("EAU02", new ReleveNumerique(3.5, "mg/L", "2026-04-28-08h", "EAU02"));

        // ====================== PRODUCTION ======================
        System.out.println("\n>>>>> ENREGISTREMENT DE PRODUCTION <<<<<\n");

        ferme.enregistrerProduction("ZE01", 120, "2026-04-25");
        ferme.enregistrerProduction("ZE01", 115, "2026-04-26");
        ferme.enregistrerProduction("ZE01", 125, "2026-04-27");
        ferme.enregistrerProduction("ZE02", 85, "2026-04-27");
        ferme.enregistrerProduction("ZA01", 50, "2026-04-27");

        // ====================== 5. GESTION DES ALERTES ======================
        System.out.println("\n>>>>> 5. PANNEAU D'ALERTES <<<<<");
        ferme.trierAlertesParGravite();
        ferme.afficherPanneauAlertes();

        // Acquitter une alerte
        ferme.acquitterAlerte(1);
        ferme.afficherAlertesActives();

        // Historique filtré
        ferme.consulterHistoriqueAlertes("ZC01", null, null, null, null);
        ferme.consulterHistoriqueAlertes(null, NiveauGravite.CRITIQUE, null, null, null);

        // ====================== TABLEAUX DE BORD ======================
        System.out.println("\n>>>>> TABLEAUX DE BORD <<<<<");
        ferme.afficherTableauDeBord("ZC01");
        ferme.afficherTableauDeBord("ZE01");
        ferme.afficherTableauDeBord("ZA01");

        // ====================== GRAPHIQUES ======================
        System.out.println("\n>>>>> VISUALISATION GRAPHIQUE <<<<<");
        ferme.afficherGraphiqueEvolution("ENV01");
        ferme.afficherGraphiqueEvolution("SOL01");
        ferme.afficherGraphiqueEvolution("BIO01");

        // ====================== SUSPENSION DE ZONE ======================
        System.out.println("\n>>>>> TEST SUSPENSION DE ZONE <<<<<\n");
        ferme.suspendreZone("ZC01");
        // Tentative de relevé sur zone suspendue : le capteur ne lit rien et aucune alerte n'est générée.
        ferme.enregistrerReleve("ENV01", new ReleveNumerique(30.0, "°C", "2026-04-30-10h", "ENV01"));
        // Réactivation
        ferme.activerZone("ZC01");
        ferme.enregistrerReleve("ENV01", new ReleveNumerique(27.0, "°C", "2026-04-30-11h", "ENV01"));

        // ====================== DÉTAILS PAR ZONE ======================
        System.out.println("\n>>>>> DÉTAILS DE CHAQUE ZONE <<<<<\n");
        zc1.afficherDetails();
        System.out.println();
        ze1.afficherDetails();
        System.out.println();
        za1.afficherDetails();

        // ====================== VUE FINALE ======================
        ferme.afficherVueEnsembleZones();

        System.out.println("\n============================================================");
        System.out.println("              FIN DE LA SIMULATION");
        System.out.println("============================================================");
    }
}
