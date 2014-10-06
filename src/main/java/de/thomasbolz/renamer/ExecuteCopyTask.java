package de.thomasbolz.renamer;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.concurrent.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@link Task} that is responsible for executing the actual renaming work. The renaming is done by copying each file to
 * the according target subdirectory and rename it.
 *
 * Created by Thomas Bolz on 07.09.14.
 */
public class ExecuteCopyTask extends Task<Renamer> {

    private final Log log = LogFactory.getLog(this.getClass());

    private final Renamer renamer;
    private final ReadOnlyDoubleWrapper dirProgress;

    public ReadOnlyDoubleProperty dirProgressProperty() {
        return dirProgress.getReadOnlyProperty();
    }


    private final ReadOnlyDoubleWrapper fileProgress;

    public ReadOnlyDoubleProperty fileProgressProperty() {
        return fileProgress.getReadOnlyProperty();
    }

    private final ReadOnlyStringWrapper progressMessage;

    public ReadOnlyStringProperty progressMessageProperty() {
        return progressMessage.getReadOnlyProperty();
    }


    public ExecuteCopyTask(Renamer renamer) {
        this.renamer = renamer;
        dirProgress = new ReadOnlyDoubleWrapper(0d);
        fileProgress = new ReadOnlyDoubleWrapper(0d);
        progressMessage = new ReadOnlyStringWrapper("");
    }


    @Override
    protected Renamer call() throws Exception {
        log.info("Start executing CopyTasks");
        long dirCounter = 0;
        long time = System.currentTimeMillis();
        for (Path dir : renamer.getCopyTasks().keySet()) {
            Path targetDir = renamer.getTarget().resolve(renamer.getSource().relativize(dir));
            try {
                if (!dir.equals(renamer.getSource())) {
                    Files.copy(dir, targetDir);
                    updateProgessMessage("renaming directory " + targetDir + "\n");
                }
            } catch (FileAlreadyExistsException e) {
                log.error("Directory already exists " + targetDir, e);
                updateProgessMessage("Directory already exists " + targetDir + "\n");
            } catch (IOException e) {
                log.error("IO Exception while trying to create directory " + targetDir, e);
            }
            long fileCounter = 0;
            for (CopyTask task : renamer.getCopyTasks().get(dir)) {
                updateFileProgress(++fileCounter, renamer.getCopyTasks().get(dir).size());
                try {
                    Files.copy(task.getSourceFile(), task.getTargetFile());
                    updateProgessMessage(task.toFormattedString() + "\n");
                } catch (FileAlreadyExistsException e) {
                    log.info("File already exists " + task.getTargetFile());
                    updateProgessMessage("File already exists " + task.getTargetFile() + "\n");
                } catch (IOException e) {
                    log.error("IO Exception while trying to execute CopyTask " + task, e);
                }
            }
            updateDirProgress(++dirCounter, renamer.getCopyTasks().size());
        }
        final long totalTime = System.currentTimeMillis() - time;
        updateProgessMessage("Finished executing CopyTasks in " + totalTime / 1000 + "s and "+totalTime%1000+"ms");
        return renamer;
    }

    private void updateProgessMessage(String s) {
        progressMessage.set(s);
    }

    private void updateDirProgress(double workDone, double max) {
        log.debug("DIR: workDon=" + workDone);
        log.debug("DIR: max=" + max);
        log.debug("DIR: rate=" + workDone / max);
        dirProgress.set(workDone / max);
    }

    private void updateDirProgress(long workDone, long max) {
        updateDirProgress((double) workDone, (double) max);
    }

    private void updateFileProgress(double workDone, double max) {
        log.debug("FILE: workDone=" + workDone);
        log.debug("FILE: max=" + max);
        log.debug("FILE: rate=" + workDone / max);
        fileProgress.set(workDone / max);
    }

    private void updateFileProgress(long workDone, long max) {
        updateFileProgress((double) workDone, (double) max);
    }


}
