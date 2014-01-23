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

/**
 * Monitors the progress of the renaming process.
 * Created by Thomas Bolz on 30.12.13.
 */
public interface ProgressListener {

    /**
     * Fired for each file of a directory.
     *
     *
     * @param progress File progress goes for each directory from 0.0 to 1.0
     */
    public void fileProgressChanged(double progress);

    /**
     * Fired for each directory found.
     *
     * @param progress Directory progress can be between 0.0 and 1.0
     */
    public void directoryProgressChanged(double progress);

    /**
     * Fired for each {@link de.thomasbolz.renamer.CopyTask}
     *
     * @param copyTask the current CopyTask
     */
    public void currentCopyTaskChanged(CopyTask copyTask);

}
