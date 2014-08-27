/*
 * Copyright 2014 Thomas Bolz
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.thomasbolz.renamer;

import javafx.application.Platform;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.stage.DirectoryChooser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.SortedMap;
import java.util.prefs.Preferences;

/**
 * Controller for the main GUI. The GUI is defined in renamer.fxml.
 */
public class RenamerGUI implements ProgressListener {

    Log log = LogFactory.getLog(this.getClass());

    private static final String SRC_DIR = "srcDir";
    private static final String TARGET_DIR = "targetDir";
    private static final String DISCLAIMER = "disclaimer";

    /**
     * Contains the source directory of the renaming operation.
     */
    private SimpleObjectProperty<File> srcDirectory = new SimpleObjectProperty<File>(null);
    private Renamer renamer;

    public File getSrcDirectory() {
        return srcDirectory.get();
    }

    public void setSrcDirectory(File value) {
        srcDirectory.set(value);
    }

    public SimpleObjectProperty<File> srcDirectory() {
        return srcDirectory;
    }

    /**
     * Contains the target directory of the renaming operation.
     */
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

    /**
     * Flag indicating if we are in simulation mode (Renamer only shows what would happen) or not (Renamer actually executes the renaming).
     */
    private BooleanProperty simulationMode = new SimpleBooleanProperty(true);

    public Boolean isSimulationMode() {
        return simulationMode.get();
    }

    public void setSimulationMode(Boolean value) {
        simulationMode.setValue(value);
    }

    public BooleanProperty simulationMode() {
        return simulationMode;
    }

    @FXML
    private Button btnSource;

    @FXML
    private Button btnTarget;

    @FXML
    private Button btnRename;

    @FXML
    private Button btnHelp;

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


    /**
     * Action handler for the target directory button.
     *
     * @param event
     */
    @FXML
    void changeTarget(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();

        if (getTargetDirectory().isDirectory()) {
            chooser.setInitialDirectory(getTargetDirectory());
        }
        File file = chooser.showDialog(txtOut.getScene().getWindow());
        if (file != null && file.isDirectory()) {
            setTargetDirectory(file);
        }
    }

    /**
     * Action handler for the source directory button.
     *
     * @param event
     */
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

    /**
     * Checks some rules about allowed source and target combinations
     *
     * @param source
     * @param target
     * @return
     */
    private boolean checkDirectories(File source, File target) {
        if (source == null) {
            Dialogs.create().lightweight().message("Source directory must be set!").showWarning();
            return false;
        } else if (target == null) {
            Dialogs.create().lightweight().message("Target directory must be set!").showWarning();
            return false;
        } else if (source.getAbsolutePath().equals(target.getAbsolutePath())) {
            Dialogs.create().lightweight().message("Source and target cannot be the same directory!").showWarning();
            return false;
        } else if (source.getAbsolutePath().startsWith(target.getAbsolutePath())) {
            Dialogs.create().lightweight().message("Source directory cannot be subdirectory of target!").showWarning();
            return false;
        } else if (target.getAbsolutePath().startsWith(source.getAbsolutePath())) {
            Dialogs.create().lightweight().message("Target directory cannot be subdirectory of source!").showWarning();
            return false;
        }
        return true;
    }

    /**
     * Action handler for the rename button
     *
     * @param event
     */
    @FXML
    void rename(ActionEvent event) {
        if (checkDirectories(getSrcDirectory(), getTargetDirectory()) == false) {
            return;
        }
        if (isSimulationMode()) {
            txtOut.clear();
            Runnable myTask = new Runnable() {
                @Override
                public void run() {
                    renamer = new Renamer(getSrcDirectory().toPath(), getTargetDirectory().toPath());
                    renamer.addProgressListener(RenamerGUI.this);
                    renamer.prepareCopyTasks();
                    final SortedMap<Path, List<CopyTask>> copyTasks = renamer.getCopyTasks();
                    StringBuilder sb = new StringBuilder();
                    copyTasks.forEach((path, tasks) -> {
                        sb.append(path);
                        sb.append("\n");
                        tasks.forEach(task -> {
                            sb.append(task.toFormattedString());
                            sb.append("\n");
                        });
                    });
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            txtOut.setText(sb.toString());
                        }
                    });
                }
            };
            final Thread taskRunner = new Thread(myTask);
//        taskRunner.setName("TaskRunner");
//        taskRunner.setDaemon(true);
            taskRunner.start();
            setSimulationMode(false);
            return;
        } else {
//            Action confirm = Dialogs.create()
//                    .title("Confirm renaming")
//                    .masthead("Do you want to execute the renaming?")
//                    .message("The author of this software is not liable for any damage that might occur to your files.")
//                    .showConfirm();
//
//            if (confirm == Dialog.Actions.YES) {
            txtOut.clear();
            Task<Void> executeTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    log.info("Running on FXThread:" + Platform.isFxApplicationThread());
                    renamer.executeCopyTasks();
                    return null;
                }
            };
            Thread th = new Thread(executeTask);
            th.setDaemon(true);
            th.start();
//            } else {
//                log.debug("not confirmed");
//            }
            setSimulationMode(true);
            return;
        }


    }

    @FXML
    void showHelp(ActionEvent event) {
        initHelp();
    }

    @FXML
    void initialize() {
        assert btnSource != null : "fx:id=\"btnSource\" was not injected: check your FXML file 'renamer.fxml'.";
        assert btnTarget != null : "fx:id=\"btnTarget\" was not injected: check your FXML file 'renamer.fxml'.";
        assert lblSource != null : "fx:id=\"lblSource\" was not injected: check your FXML file 'renamer.fxml'.";
        assert lblTarget != null : "fx:id=\"lblTarget\" was not injected: check your FXML file 'renamer.fxml'.";
        assert progressDirs != null : "fx:id=\"progressDirs\" was not injected: check your FXML file 'renamer.fxml'.";
        assert progressFiles != null : "fx:id=\"progressFiles\" was not injected: check your FXML file 'renamer.fxml'.";
        assert txtOut != null : "fx:id=\"txtOut\" was not injected: check your FXML file 'renamer.fxml'.";
        setSimulationMode(true);
        initBindings();
        initFromPrefs();
        initHelp();
        checkDisclaimer();
    }

    private void initHelp() {
        InputStream in = getClass().getResourceAsStream("help.txt");
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            txtOut.setText(sb.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void checkDisclaimer() {
        if (!isDisclaimerConfirmed()) {
            Dialog.Actions.YES.textProperty().set("Yes, use at my risk");
            Dialog.Actions.NO.textProperty().set("No way");
            Action action = Dialogs.create()
                    .title("Disclaimer")
                    .masthead("Do you want to use this software at your own risk?")
                    .message("This software is done with care and was tested thoroughly. Nevertheless, the use of this software is completely at your own risk. The author of this software is not liable for any loss or damage that might occur to your data.")
                    .showConfirm();
            if (action != Dialog.Actions.YES) {
                System.exit(0);
            } else {
                confirmDisclaimer();
            }
        }
    }

    private void initFromPrefs() {
        setSrcDirectory(new File(getSourceDirectoryFromPrefs()));
        setTargetDirectory(new File(getTargetDirectoryFromPrefs()));
    }

    /**
     * Initialize the property bindings.
     */
    private void initBindings() {

        // reflect changes of srcDirectory in the GUI and store the new source dir to the preferences
        srcDirectory.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observableValue, File oldFile, File newFile) {
                if (newFile != null) {
                    String path = newFile.getAbsolutePath();
                    lblSource.setText(path);
                    setSourceDirectoryToPrefs(path);
                }
                setSimulationMode(true);
            }
        });
        // reflect changes of targetDirectory in the GUI and store the new target dir to the preferences
        targetDirectory.addListener(new ChangeListener<File>() {
            @Override
            public void changed(ObservableValue<? extends File> observableValue, File oldFile, File newFile) {
                if (newFile != null) {
                    String path = newFile.getAbsolutePath();
                    lblTarget.setText(path);
                    setTargetDirectoryToPrefs(path);
                }
                setSimulationMode(true);
            }
        });
        btnRename.textProperty().bind(new When(simulationMode).then("Simulate renaming").otherwise("Execute renaming"));
    }

    private void setSourceDirectoryToPrefs(String path) {
        final Preferences preferences = getPreferences();
        preferences.put(SRC_DIR, path);
    }

    /**
     * Retrieves the source directory from the preferences, if not available use the user's home directory
     *
     * @return
     */
    private String getSourceDirectoryFromPrefs() {
        final Preferences preferences = getPreferences();
        return preferences.get(SRC_DIR, System.getProperty("user.home"));
    }

    private void setTargetDirectoryToPrefs(String path) {
        final Preferences preferences = getPreferences();
        preferences.put(TARGET_DIR, path);
    }

    private boolean isDisclaimerConfirmed() {
        final Preferences preferences = getPreferences();
        return preferences.getBoolean(DISCLAIMER, false);
    }

    private void confirmDisclaimer() {
        final Preferences preferences = getPreferences();
        preferences.putBoolean(DISCLAIMER, true);
    }

    /**
     * Retrieves the target directory from the preferences, if not available use the user's home directory
     *
     * @return
     */
    private String getTargetDirectoryFromPrefs() {
        final Preferences preferences = getPreferences();
        return preferences.get(TARGET_DIR, System.getProperty("user.home"));
    }

    private Preferences getPreferences() {
        return Preferences.userNodeForPackage(getClass());
    }

    @Override
    public void directoryProgressChanged(final double progress) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progressDirs.setProgress(progress);
            }
        });
    }

    @Override
    public void fileProgressChanged(final double progress) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progressFiles.setProgress(progress);
            }
        });
    }

    @Override
    public void currentCopyTaskChanged(final CopyTask copyTask) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                txtOut.appendText(copyTask.toFormattedString() + "\n");
            }
        });
    }

    void resetPreferences() {
        getPreferences();
        getPreferences().remove(SRC_DIR);
        getPreferences().remove(TARGET_DIR);
        getPreferences().remove(DISCLAIMER);
    }
}
