package ferme.ui.viewmodels;

public class AquacultureZoneViewModel {
    private final String code;
    private final String name;
    private final String speciesSummary;
    private final int totalAnimals;
    private final String feedType;
    private final String quantityPerMeal;
    private final String mealsPerDay;
    private final double harvestAverage;

    public AquacultureZoneViewModel(String code, String name, String speciesSummary, int totalAnimals,
            String feedType, String quantityPerMeal, String mealsPerDay, double harvestAverage) {
        this.code = code;
        this.name = name;
        this.speciesSummary = speciesSummary;
        this.totalAnimals = totalAnimals;
        this.feedType = feedType;
        this.quantityPerMeal = quantityPerMeal;
        this.mealsPerDay = mealsPerDay;
        this.harvestAverage = harvestAverage;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSpeciesSummary() {
        return speciesSummary;
    }

    public int getTotalAnimals() {
        return totalAnimals;
    }

    public String getFeedType() {
        return feedType;
    }

    public String getQuantityPerMeal() {
        return quantityPerMeal;
    }

    public String getMealsPerDay() {
        return mealsPerDay;
    }

    public double getHarvestAverage() {
        return harvestAverage;
    }
}
