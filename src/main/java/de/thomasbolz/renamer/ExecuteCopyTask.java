package de.thomasbolz.renamer;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Thomas Bolz on 07.09.14.
 */
public class ExecuteCopyTask extends Task<Renamer> {

    private final Log log = LogFactory.getLog(this.getClass());

    private final Renamer renamer;
    private StringBuilder textLog;
    private ReadOnlyDoubleWrapper dirProgress;
    public ReadOnlyDoubleProperty dirProgressProperty() {
        return dirProgress.getReadOnlyProperty();
    }



    private ReadOnlyDoubleWrapper fileProgress;
    public ReadOnlyDoubleProperty fileProgressProperty() {
        return fileProgress.getReadOnlyProperty();
    }


    public ExecuteCopyTask(Renamer renamer) {
        this.renamer = renamer;
        dirProgress = new ReadOnlyDoubleWrapper(0d);
        fileProgress = new ReadOnlyDoubleWrapper(0d);
        textLog = new StringBuilder();
    }


    @Override
    protected Renamer call() throws Exception {
        log.info("Start executing CopyTasks");
        long dirCounter=0;
        for (Path dir : renamer.getCopyTasks().keySet()) {
            Path targetDir = renamer.getTarget().resolve(renamer.getSource().relativize(dir));
            try {
                if (!dir.equals(renamer.getSource())) {
                    Files.copy(dir, targetDir);
                    textLog.append("renaming directory " + targetDir+"\n");
                }
            } catch (FileAlreadyExistsException e) {
                log.error("Directory already exists " + targetDir, e);
                textLog.append("Directory already exists " + targetDir+"\n");
            } catch (IOException e) {
                log.error("IO Exception while trying to create directory " + targetDir, e);
            }
            updateMessage(textLog.toString());
            long fileCounter=0;
            for (CopyTask task : renamer.getCopyTasks().get(dir)) {
                updateFileProgress(++fileCounter, renamer.getCopyTasks().get(dir).size());
                try {
//                    updateCurrentCopyTask(task);
                    Files.copy(task.getSourceFile(), task.getTargetFile());
                    textLog.append(task.toFormattedString()+"\n");
                } catch (FileAlreadyExistsException e) {
                    log.info("File already exists " + task.getTargetFile());
                    textLog.append("File already exists " + task.getTargetFile()+"\n");
                } catch (IOException e) {
                    log.error("IO Exception while trying to execute CopyTask " + task, e);
                }
                updateMessage(textLog.toString());
            }
            updateDirProgress(++dirCounter, renamer.getCopyTasks().size());
        }
        log.info("Finished executing CopyTasks");
        return renamer;
    }

    private void updateDirProgress(double workDone, double max) {
        log.debug("DIR: workDon="+workDone);
        log.debug("DIR: max="+max);
        log.debug("DIR: rate="+workDone/max);
        dirProgress.set(workDone / max);
    }

    private void updateDirProgress(long workDone, long max) {
        updateDirProgress((double) workDone, (double) max);
    }

    private void updateFileProgress(double workDone, double max) {
        log.debug("FILE: workDon="+workDone);
        log.debug("FILE: max="+max);
        log.debug("FILE: rate="+workDone/max);
        fileProgress.set(workDone / max);
    }

    private void updateFileProgress(long workDone, long max) {
        updateFileProgress((double) workDone, (double) max);
    }


}
