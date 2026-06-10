package ferme.ui.services;

import java.io.File;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileImportService {
    private final Stage owner;

    public FileImportService(Stage owner) {
        this.owner = owner;
    }

    public File pickFile(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        return chooser.showOpenDialog(owner);
    }

    public List<File> pickFiles(String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        return chooser.showOpenMultipleDialog(owner);
    }
}
