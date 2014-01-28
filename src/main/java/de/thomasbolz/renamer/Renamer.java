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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * The Renamer is responsible for all steps in the renaming process:
 * <ol>
 * <li>{@link #prepareCopyTasks()} analyzes the source and target directories and creates a list of {@link de.thomasbolz.renamer.CopyTask}s</li>
 * <li>{@link #executeCopyTasks()} executes the list of CopyTasks file by file and directory by directory.</li>
 * </ol>
 * Created by Thomas Bolz on 28.12.13.
 */
public class Renamer {

    private final Log log = LogFactory.getLog(this.getClass());
    private final Path source;
    private final Path target;
    private final SortedMap<Path, List<CopyTask>> copyTasks;
    private final static String EXLUSION_REGEX = "\\..*|thumbs.db|.*\\.ini|\\.ds.*";
    private final static String INCLUSION_LIST = ".*\\.png|.*\\.jpg|.*\\.jpeg|.*\\.mov|.*\\.avi|";
    private final List<Path> excludedFiles;
    private final List<ProgressListener> progressListeners;
    private double numberOfDirectories;

    /**
     * @return a list of all CopyTask that were found during the analysis.
     */
    public SortedMap<Path, List<CopyTask>> getCopyTasks() {
        return copyTasks;
    }

    /**
     * @return a list of all files or directories that were excluded during the analysis.
     */
    public List<Path> getExcludedFiles() {
        return excludedFiles;
    }

    /**
     * Creates a new Renamer.
     *
     * @param source directory from which the files shall be copied
     * @param target directory to which the files shall be copied
     */
    public Renamer(Path source, Path target) {
        this.source = source;
        this.target = target;
        copyTasks = new TreeMap<>();
        excludedFiles = new ArrayList();
        progressListeners = new ArrayList<>();
    }

    /**
     * Step 1 of the renaming process:
     * Analysis steps in the renaming process. Walks the whole file tree of the source directory and creates a CopyTask
     * for each file or directory that is found and does not match the exclusion list.
     * @return a list of all CopyTasks
     */
    public Map<Path, List<CopyTask>> prepareCopyTasks() {


        try {
            Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
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
                            Path targetdir = target.resolve(source.relativize(dir));
                            copyTasks.put(dir, new ArrayList<CopyTask>());
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            if (!file.getFileName().toString().toLowerCase().matches(EXLUSION_REGEX)) {
                                copyTasks.get(file.getParent()).add(new CopyTask(file, null));
                            } else {
                                excludedFiles.add(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        sortCopyTasks();
        generateTargetFilenames();
        return copyTasks;
    }

    /**
     * Sorts all CopyTasks in alphabetical order. This step must be done, as {@link java.nio.file.Files#walkFileTree(java.nio.file.Path, java.nio.file.FileVisitor)}
     * does not walk the file tree in any deterministic order.
     */
    private void sortCopyTasks() {
        for (Path path : copyTasks.keySet()) {
            final List<CopyTask> copyTasks = this.copyTasks.get(path);
            Collections.sort(copyTasks, new Comparator<CopyTask>() {
                @Override
                public int compare(CopyTask o1, CopyTask o2) {
                    return o1.getSourceFile().compareTo(o2.getSourceFile());
                }
            });
        }
    }

    /**
     * The target file names are the name of the parent directory with an index number suffix.
     */
    private void generateTargetFilenames() {
        log.debug("FXThread: " + Platform.isFxApplicationThread());
        updateDirectoryProgress(0);
        updateFileProgress(0);
        int dirCounter = 1;
        for (Path path : copyTasks.keySet()) {
            int fileCounter = 1;
            updateDirectoryProgress(dirCounter / getNumberOfDirectories());
            updateFileProgress(0);
            for (CopyTask copyTask : copyTasks.get(path)) {
                updateFileProgress(fileCounter / ((double) copyTasks.get(path).size()));
                String targetFilename = path.getFileName() + "_" + String.format("%02d", fileCounter) + "." + copyTask.getSourceFile().toString().substring(copyTask.getSourceFile().toString().lastIndexOf('.') + 1).toLowerCase();
                copyTask.setTargetFile(target.resolve(source.relativize(Paths.get(path.toString(), targetFilename))));
                fileCounter++;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            dirCounter++;
        }

    }


    /**
     * Step 2 of the renaming process:
     * Executes the list of CopyTasks.
     */
    public void executeCopyTasks() {
        log.info("Start executing CopyTasks");
        for (Path dir : copyTasks.keySet()) {
            Path targetDir = target.resolve(source.relativize(dir));
            try {
                if (!dir.equals(source)) {
                    Files.copy(dir, targetDir);
                }
            } catch (FileAlreadyExistsException e) {
                log.error("Directory already exists " + targetDir, e);
            } catch (IOException e) {
                log.error("IO Exception while trying to create directory " + targetDir, e);
            }
            for (CopyTask task : copyTasks.get(dir)) {
                try {
                    updateCurrentCopyTask(task);
                    Files.copy(task.getSourceFile(), task.getTargetFile());
                } catch (FileAlreadyExistsException e) {
                    log.info("File already exists " + task.getTargetFile());
                } catch (IOException e) {
                    log.error("IO Exception while trying to execute CopyTask " + task, e);
                }
            }
        }
        log.info("Finished executing CopyTasks");
    }

    /**
     * Returns the total number of directories, that were found during step 1.
     * @return
     */
    public double getNumberOfDirectories() {
        return (double) copyTasks.size();
    }

//    private class DefaultFileFilter implements FileFilter {
//
//
//        @Override
//        public boolean accept(File file) {
//            if (file != null && !file.getName().matches(EXLUSION_REGEX)) {
//                return true;
//            } else {
//                return false;
//            }
//        }
//    }

    /**
     * Adds a ProgressListener to this Renamer. All ProgressListeners are notified on the progress during the execution tasks.
     * @param listener
     */
    public void addProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
    }

    /**
     * Removes a ProgressListener from this Renamer.
     * @param listener
     */
    public void removeProgressListener(ProgressListener listener) {
        progressListeners.remove(listener);
    }

    /**
     * Notify all ProgressListeners about the directory progress.
     * @param progress
     */
    private void updateDirectoryProgress(double progress) {
        for (ProgressListener listener : progressListeners) {
            listener.directoryProgressChanged(progress);
        }
    }

    /**
     * Notify all ProgressListeners about the file progress.
     * @param progress
     */
    private void updateFileProgress(double progress) {
        for (ProgressListener listener : progressListeners) {
            listener.fileProgressChanged(progress);
        }
    }

    /**
     * Notify all ProgressListeners about hte current CopyTask.
     * @param copyTask
     */
    private void updateCurrentCopyTask(CopyTask copyTask) {
        for (ProgressListener listener : progressListeners) {
            listener.currentCopyTaskChanged(copyTask);
        }
    }


}
