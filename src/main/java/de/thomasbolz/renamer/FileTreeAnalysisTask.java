package de.thomasbolz.renamer;

import javafx.application.Platform;
import javafx.concurrent.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * Created by Thomas Bolz on 07.09.14.
 */
public class FileTreeAnalysisTask extends Task<Renamer> {

    private final Log log = LogFactory.getLog(this.getClass());

    private final Renamer renamer;

    public FileTreeAnalysisTask(Renamer renamer) {
        this.renamer = renamer;
    }

    @Override
    protected Renamer call() throws Exception {
        try {
            updateMessage("Analyzing directories:");
            log.debug("analyzing directories");
            Files.walkFileTree(renamer.getSource(), EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {

                        /**
                         * If we find a directory create/copy it and add a counter
                         *
                         * @param dir
                         * @param attrs
                         * @return
                         * @throws java.io.IOException
                         */
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                                throws IOException {
                            Path targetdir = renamer.getTarget().resolve(renamer.getSource().relativize(dir));
                            renamer.getCopyTasks().put(dir, new ArrayList<CopyTask>());
                            log.info("dir=" + dir);
                            updateMessage("Analyzing " + dir.toString());
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            if (!file.getFileName().toString().toLowerCase().matches(renamer.getExclusionRegEx())) {
                                CopyTask copyTask = new CopyTask(file, null);
                                renamer.getCopyTasks().get(file.getParent()).add(copyTask);
                            } else {
                                renamer.getExcludedFiles().add(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
            log.error("IOException during directory analysis", e);
        }
        updateMessage("Sorting files");
        log.debug("sorting files");
        renamer.sortCopyTasks();
        updateMessage("Files sorted");
        Thread.sleep(100);
        updateMessage("Generate target filenames");
        log.debug("generating filenames");
        generateTargetFilenames();
        updateMessage("Target file names generated");
        Thread.sleep(100);
        updateMessage("# of files to rename: " + renamer.getNumberOfFiles() + " in " + renamer.getNumberOfDirectories() + " directories");
        return renamer;
    }

    /**
     * The target file names are the name of the parent directory with an index number suffix.
     */
    void generateTargetFilenames() {
        log.debug("FXThread: " + Platform.isFxApplicationThread());
        int dirCounter = 1;
        for (Path path : renamer.getCopyTasks().keySet()) {
            int fileCounter = 1;
            // get number of files to determine number of index digits
            final long numberOfFiles = ((Integer) renamer.getCopyTasks().get(path).size()).toString().length();
            String indexFormat = "%0" + numberOfFiles + "d";
            for (CopyTask copyTask : renamer.getCopyTasks().get(path)) {
                String targetFilename = path.getFileName() + "_" + String.format(indexFormat, fileCounter) + "." + copyTask.getSourceFile().toString().substring(copyTask.getSourceFile().toString().lastIndexOf('.') + 1).toLowerCase();
                copyTask.setTargetFile(renamer.getTarget().resolve(renamer.getSource().relativize(Paths.get(path.toString(), targetFilename))));
                fileCounter++;
            }
            dirCounter++;
        }

    }

}
