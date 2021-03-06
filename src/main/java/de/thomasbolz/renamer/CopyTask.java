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

import java.nio.file.Path;

/**
 * A CopyTask contains the information for a single copy operation (either for a file or a directory).
 * Created by Thomas Bolz on 28.12.13.
 */
public class CopyTask {
    private Path sourceFile;
    private Path targetFile;

    public void setTargetFile(Path targetFile) {
        this.targetFile = targetFile;
    }

    public Path getSourceFile() {
        return sourceFile;
    }

    public Path getTargetFile() {
        return targetFile;
    }

    public CopyTask(Path sourceFile, Path targetFile) {
        this.sourceFile = sourceFile;
        this.targetFile = targetFile;
    }

    @Override
    public String toString() {
        return "CopyTask{" +
                "sourceFile=" + sourceFile +
                " -> targetFile=" + targetFile +
                '}';
    }

    /**
     * Returns a formatted string that documents this CopyTask.
     *
     * @return
     */
    public String toFormattedString() {
        return sourceFile + "\t-->\t" + targetFile;
    }
}
