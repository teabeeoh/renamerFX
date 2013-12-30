/*
 * Copyright 2013 Thomas Bolz
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by Thomas Bolz on 28.12.13.
 */
public class Renamer {

    private final Log log = LogFactory.getLog(this.getClass());
    private final Path source;
    private final Path target;
    private final SortedMap<Path, List<CopyTask>> copyTasks;
    private final static String EXLUSION_REGEX = "\\..*";
    private final List<Path> excludedFiles;

    public SortedMap<Path, List<CopyTask>> getCopyTasks() {
        return copyTasks;
    }

    public List<Path> getExcludedFiles() {
        return excludedFiles;
    }

    public Renamer(Path source, Path target) {
        this.source = source;
        this.target = target;
        copyTasks = new TreeMap<>();
        excludedFiles = new ArrayList();
    }

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
                            if (!file.getFileName().toString().matches(EXLUSION_REGEX)) {
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

    private void generateTargetFilenames() {
        for (Path path : copyTasks.keySet()) {
            int counter = 1;
            for (CopyTask copyTask : copyTasks.get(path)) {
                String targetFilename = path.getFileName() + "_" + String.format("%02d", counter) + "." + copyTask.getSourceFile().toString().substring(copyTask.getSourceFile().toString().lastIndexOf('.') + 1).toLowerCase();
                copyTask.setTargetFile(target.resolve(source.relativize(Paths.get(path.toString(), targetFilename))));
                counter++;
            }
        }

    }

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
                    Files.copy(task.getSourceFile(), task.getTargetFile());
                } catch (FileAlreadyExistsException e) {
                    log.error("File already exists " + task.getTargetFile(), e);
                } catch (IOException e) {
                    log.error("IO Exception while trying to execute CopyTask " + task, e);
                }
            }
        }
        log.info("Finished executing CopyTasks");
    }

    private class DefaultFileFilter implements FileFilter {


        @Override
        public boolean accept(File file) {
            if (file != null && !file.getName().matches(EXLUSION_REGEX)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
