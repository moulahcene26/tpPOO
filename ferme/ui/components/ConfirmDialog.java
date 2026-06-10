package ferme.ui.components;

import ferme.ui.services.DialogService;

public final class ConfirmDialog {
    private ConfirmDialog() {
    }

    public static boolean show(DialogService dialogs, String title, String message) {
        return dialogs.confirm(title, message);
    }
}
