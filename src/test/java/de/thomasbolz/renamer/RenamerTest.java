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
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * Created by Thomas Bolz on 28.12.13.
 */
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
        log.debug(pathListMap);
        renamer.executeCopyTasks();
        log.debug(renamer.getExcludedFiles());

    }
}
