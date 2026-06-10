package ferme.ui;

import ferme.ui.navigation.NavigationController;
import ferme.ui.services.DialogService;
import ferme.ui.services.FileImportService;
import ferme.ui.services.UiFarmService;

public interface AppContextAware {
    void setContext(AppContext context,
            UiFarmService farmService,
            DialogService dialogService,
            FileImportService fileImportService,
            NavigationController navigationController);
}
