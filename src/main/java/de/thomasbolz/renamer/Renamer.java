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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Created by Thomas Bolz on 28.12.13.
 */
public class Renamer {

    private Log log = LogFactory.getLog(this.getClass());
    private Path source;
    private Path target;

    public Renamer(Path source, Path target) {
        this.source = source;
        this.target = target;
    }

    public Map<Path, List<CopyTask>> prepareCopyTasks() {
        final Map<Path, List<CopyTask>> result = new HashMap<>();

        final Map<Path, Integer> counters = new HashMap<>();
        try {
            Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                    new SimpleFileVisitor<Path>() {

                        String currentDirectoryName = "";
                        String fileNamePattern = "";

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
                            counters.put(dir, new Integer(1));
                            result.put(dir, new ArrayList<CopyTask>());
                            result.get(dir).add(new CopyTask(dir, targetdir));
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                throws IOException {
                            Path parentPath = file.getParent();
                            Path targetFile;
                            String filename = parentPath.getFileName() + "_" + String.format("%02d", counters.get(parentPath)) + "." + file.toString().substring(file.toString().lastIndexOf('.') + 1).toLowerCase();
                            counters.put(parentPath, counters.get(parentPath) + 1);
                            targetFile = target.resolve(source.relativize(Paths.get(parentPath.toString(), filename)));
                            result.get(parentPath).add(new CopyTask(file, targetFile));
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public void executeCopyTasks(Map<Path, List<CopyTask>> pathListMap) {
        for (Path parentPath : pathListMap.keySet()) {
            for (CopyTask task : pathListMap.get(parentPath)) {
                try {
                    Files.copy(task.getSourceFile(), task.getTargetFile());
                } catch (FileAlreadyExistsException e) {
                    log.error("File/Directory already exists " + task.getTargetFile(), e);
                } catch (IOException e) {
                    log.error("IO Exception while trying to execute CopyTask " + task, e);
                    System.exit(0);
                }
            }
        }
    }

}
