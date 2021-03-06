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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class RenamerTest {

    private Log log = LogFactory.getLog(this.getClass());

    private Path source;
    private Path target;

    @Before
    public void setUp() throws Exception {
        source = Paths.get(new File(".").getCanonicalPath(), "source");
        target = Paths.get(new File(".").getCanonicalPath(), "target");
        if (!target.toFile().exists() || target.toFile().listFiles().length <= 5) {
            FileUtils.deleteDirectory(target.toFile());
            Files.createDirectories(target);
        }
        log.debug(source);
        log.debug(target);


    }

    @After
    public void tearDown() throws Exception {
        if (!target.toFile().exists() || target.toFile().listFiles().length <= 5) {
//            FileUtils.deleteDirectory(target.toFile());
        }
    }

    @Test
    public void testPrepareCopyTasks() throws Exception {

        log.debug("\n*** testPrepareCopyTasks ***\n");
        Renamer renamer = new Renamer(source, target);
        final Map<Path, List<CopyTask>> pathListMap = renamer.prepareCopyTasks();
        for (Path path : pathListMap.keySet()) {
            log.debug("Copying the following files for path " + path);
            for (CopyTask copyTask : pathListMap.get(path)) {
                log.debug(copyTask);
            }
        }
//        log.debug(pathListMap);
//
//        log.debug(renamer.getExcludedFiles());
        Assert.assertEquals(3, renamer.getExcludedFiles().size());
        for (Path path : renamer.getExcludedFiles()) {
            Assert.assertTrue(isExcludedFile(path.toString()));
        }

    }

    private boolean isExcludedFile(String s) {
        return s.contains(".file1.txt") || s.contains("Thumbs.db") || s.contains(".DS_Store");
    }

    @Test
    public void testExcecuteCopyTasks() throws Exception {

        log.debug("\n*** testExcecuteCopyTasks ***\n");
        Renamer renamer = new Renamer(source, target);
        final Map<Path, List<CopyTask>> pathListMap = renamer.prepareCopyTasks();
        renamer.executeCopyTasks();

        log.debug(renamer.getExcludedFiles());
        final File[] files = Paths.get(new File(".").getCanonicalPath(), "target", "dir1", "subdir").toFile().listFiles();

        Assert.assertEquals(7, files.length);
        for (File file : files) {
            Assert.assertTrue(isIncludedFile(file.toString()));
        }

    }

    private boolean isIncludedFile(String s) {
        log.debug("filename=" + s);
        return s.contains("subdir_01.txt") || s.contains("subdir_02.txt") || s.contains("subdir_03.avi") || s.contains("subdir_04.jpeg") || s.contains("subdir_05.jpg") || s.contains("subdir_06.mov") || s.contains("subdir_07.png");
    }

}
