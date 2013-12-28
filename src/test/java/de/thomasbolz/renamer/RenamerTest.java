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

    Path source;
    Path target;

    @Before
    public void setUp() throws Exception {
        source = Paths.get(new File(".").getCanonicalPath(), "source");
        target = Paths.get(new File(".").getCanonicalPath(), "target");
        if (target.toFile().listFiles().length <= 5) {
            FileUtils.deleteDirectory(target.toFile());
            Files.createDirectories(target);
        }
//        Files.copy(Paths.get("/Users/cgi/Documents/workspaces/idea/renamerFX/source/dir1/dir1_3"), Paths.get("/Users/cgi/Documents/workspaces/idea/renamerFX/target/dir1/dir1_3"));
        System.out.println(source);
        System.out.println(target);
//        System.exit(0);


    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testPrepareCopyTasks() throws Exception {

        System.out.println("\n*** testPrepareCopyTasks ***\n");
        Renamer renamer = new Renamer(source, target);
        final Map<Path, List<CopyTask>> pathListMap = renamer.prepareCopyTasks();
        for (Path path : pathListMap.keySet()) {
            System.out.println("Copying the following files for path " + path);
            for (CopyTask copyTask : pathListMap.get(path)) {
                System.out.println(copyTask);
            }
        }
        System.out.println(pathListMap);
        renamer.executeCopyTasks(pathListMap);

    }
}
