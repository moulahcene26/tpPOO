package ferme.interfaces;

import ferme.models.Animal;
import ferme.enums.EtatSante;

public interface IGestionAnimaux {
    boolean enregistrerAnimal(Animal animal);
    boolean mettreAJourSante(int numeroAnimal, EtatSante nouvelEtat);
    boolean enregistrerEvenementSanitaire(int numeroAnimal, String description, String date);
    boolean enregistrerEvolutionPoids(int numeroAnimal, double nouveauPoids, String date);
    void afficherAnimauxParEtatSante(String codeZone, EtatSante etat);
    void afficherProgrammeAlimentation(String codeZone);
}
