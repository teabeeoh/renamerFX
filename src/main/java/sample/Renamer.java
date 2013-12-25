package sample;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;


public class Renamer {

    Log log = LogFactory.getLog(this.getClass());

    public static final String SRC_DIR = "srcDir";
    private static final String TARGET_DIR = "targetDir";

    private SimpleObjectProperty<File> srcDirectory = new SimpleObjectProperty<File>(null);

    public File getSrcDirectory() {
        return srcDirectory.get();
    }

    public void setSrcDirectory(File value) {
        srcDirectory.set(value);
    }

    public SimpleObjectProperty<File> srcDirectory() {
        return srcDirectory;
    }

    private SimpleObjectProperty<File> targetDirectory = new SimpleObjectProperty<File>(null);

    public File getTargetDirectory() {
        return targetDirectory.get();
    }

    public void setTargetDirectory(File value) {
        targetDirectory.set(value);
    }

    public SimpleObjectProperty<File> targetDirectory() {
        return targetDirectory;
    }

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button btnSource;

    @FXML
    private Button btnTarget;

    @FXML
    private Button btnRename;

    @FXML
    private Label lblDirs;

    @FXML
    private Label lblFiles;

    @FXML
    private Label lblSource;

    @FXML
    private Label lblTarget;

    @FXML
    private ProgressBar progressDirs;

    @FXML
    private ProgressBar progressFiles;

    @FXML
    private TextArea txtOut;


    @FXML
    void changeSrc(ActionEvent event) {
        DirectoryChooser f = new DirectoryChooser();

        if (getSrcDirectory().isDirectory()) {
            f.setInitialDirectory(getSrcDirectory());
        }
        File file = f.showDialog(txtOut.getScene().getWindow());
        if (file != null && file.isDirectory()) {
            setSrcDirectory(file);
        }
    }

    @FXML
    void changeTarget(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();

        if (getTargetDirectory().isDirectory()) {
            chooser.setInitialDirectory(getSrcDirectory());
        }
        File file = chooser.showDialog(txtOut.getScene().getWindow());
        if (file != null && file.isDirectory()) {
            setTargetDirectory(file);
        }
    }

    @FXML
    void rename(ActionEvent event) {
        System.out.println("Renaming files");
        txtOut.clear();
        final Path source = getSrcDirectory().toPath();
        final Path target = getTargetDirectory().toPath();
        log.info("source path: " + source);
        log.info("target path: " + target);
        try {
            Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {

                        String currentDirectoryName = "";
                        String fileNamePattern = "";
                        int counter = 1;

                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                throws IOException {
                            Path targetdir = target.resolve(source.relativize(dir));
                            currentDirectoryName = dir.getFileName().toString();
                            log.info("**** current directory: " + currentDirectoryName);
                            try {
                                Files.copy(dir, targetdir);
                                String message = "directory copyied from " + dir + " to " + targetdir;
                                log.info(message);
                                txtOut.appendText(message + "\n");
                            } catch (FileAlreadyExistsException e) {
//                                if (!Files.isDirectory(targetdir)) {
//                                    System.out.println("File is directory");
//                                }
//                                    throw e;
                                String message = "directory " + dir + " already exists in " + targetdir;
                                log.info(message);
                                txtOut.appendText(message + "\n");
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            try {
                                String filename = currentDirectoryName + "_" + String.format("%02d", counter++);
                                log.debug("++ " + filename);
//                                file = Paths.get(file.getParent().toString(), file)
                                Files.copy(file, target.resolve(source.relativize(file)));
                                String message = "copied from " + file + " to " + target.resolve(source.relativize(file));
                                log.info(message);
                                txtOut.appendText(message + "\n");
                            } catch (FileAlreadyExistsException e) {
                                String message = "file " + file + " already exists";
                                log.info(message);
                                txtOut.appendText(message + "\n");
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
//        if (srcDirectory != null && srcDirectory.isDirectory()) {
//            walkFileTree(srcDirectory);
//        }

    }

    private void walkFileTree(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                walkFileTree(file);
            } else {
                txtOut.appendText(file.getPath() + "\n");
                Paths.get(file.toURI());
            }

        }
    }

    @FXML
    void initialize() {
        assert btnSource != null : "fx:id=\"btnSource\" was not injected: check your FXML file 'renamer.fxml'.";
        assert btnTarget != null : "fx:id=\"btnTarget\" was not injected: check your FXML file 'renamer.fxml'.";
        assert lblDirs != null : "fx:id=\"lblDirs\" was not injected: check your FXML file 'renamer.fxml'.";
        assert lblFiles != null : "fx:id=\"lblFiles\" was not injected: check your FXML file 'renamer.fxml'.";
        assert lblSource != null : "fx:id=\"lblSource\" was not injected: check your FXML file 'renamer.fxml'.";
        assert lblTarget != null : "fx:id=\"lblTarget\" was not injected: check your FXML file 'renamer.fxml'.";
        assert progressDirs != null : "fx:id=\"progressDirs\" was not injected: check your FXML file 'renamer.fxml'.";
        assert progressFiles != null : "fx:id=\"progressFiles\" was not injected: check your FXML file 'renamer.fxml'.";
        assert txtOut != null : "fx:id=\"txtOut\" was not injected: check your FXML file 'renamer.fxml'.";
        initBindings();
        initFromPrefs();
    }

    private void initFromPrefs() {
        setSrcDirectory(new File(getSourceDirectoryToPrefs()));
        setTargetDirectory(new File(getTargetDirectoryToPrefs()));

    }

    private void initBindings() {
        srcDirectory.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observableValue, File oldFile, File newFile) {
                if (newFile != null && newFile.isDirectory()) {
                    String path = newFile.getAbsolutePath();
                    lblSource.setText(path);
                    setSourceDirectoryFromPrefs(path);
                }
            }
        });
        targetDirectory.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observableValue, File oldFile, File newFile) {
                if (newFile != null && newFile.isDirectory()) {
                    String path = newFile.getAbsolutePath();
                    lblTarget.setText(path);
                    setTargetDirectoryFromPrefs(path);
                }
            }
        });
    }

    private void setSourceDirectoryFromPrefs(String path) {
        final Preferences preferences = getPreferences();
        preferences.put(SRC_DIR, path);
    }

    private String getSourceDirectoryToPrefs() {
        final Preferences preferences = getPreferences();
        return preferences.get(SRC_DIR, System.getProperty("user.home"));
    }

    private void setTargetDirectoryFromPrefs(String path) {
        final Preferences preferences = getPreferences();
        preferences.put(TARGET_DIR, path);
    }

    private String getTargetDirectoryToPrefs() {
        final Preferences preferences = getPreferences();
        return preferences.get(TARGET_DIR, System.getProperty("user.home"));
    }

    private Preferences getPreferences() {
        return Preferences.userNodeForPackage(getClass());
    }

}
