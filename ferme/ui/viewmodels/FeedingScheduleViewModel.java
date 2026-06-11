package ferme.ui.viewmodels;

public class FeedingScheduleViewModel {
    private final String zoneCode;
    private final String category;
    private final String feedType;
    private final String quantityPerMeal;
    private final int mealsPerDay;

    public FeedingScheduleViewModel(String zoneCode, String category, String feedType,
            String quantityPerMeal, int mealsPerDay) {
        this.zoneCode = zoneCode;
        this.category = category;
        this.feedType = feedType;
        this.quantityPerMeal = quantityPerMeal;
        this.mealsPerDay = mealsPerDay;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public String getCategory() {
        return category;
    }

    public String getFeedType() {
        return feedType;
    }

    public String getQuantityPerMeal() {
        return quantityPerMeal;
    }

    public int getMealsPerDay() {
        return mealsPerDay;
    }
}
