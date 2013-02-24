/* MyMAM - Open Source Digital Media Asset Management.
 * http://www.mymam.net
 *
 * Copyright 2013, MyMAM contributors as indicated by the @author tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mymam.fileprocessor;

import net.mymam.data.json.MediaFile;
import net.mymam.fileprocessor.exceptions.FileAlreadyInProgressException;
import net.mymam.fileprocessor.exceptions.FileProcessingFailedException;
import net.mymam.fileprocessor.exceptions.RestCallFailedException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.io.File;
import java.util.List;

/**
 * @author fstab
 */
@DisallowConcurrentExecution
public class DeleteJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
//        try {
//            Config config = Config.fromJobDataMap(jobExecutionContext.getMergedJobDataMap());
//            RestClient restClient = new RestClient(config);
//            List<MediaFile> filesToBeDeleted = restClient.loadFilesMarkedForDeletion();
//            for ( MediaFile file : filesToBeDeleted ) {
//                deleteFile(file, restClient, config);
//            }
//        } catch (RestCallFailedException e) {
//            // TODO: Log as error
//            System.out.println("Failed to load file list from server.");
//            e.printStackTrace();
//        }
    }
//
//    private void deleteFile(MediaFile file, RestClient restClient, Config config) {
//        try {
//            restClient.setDeletionInProgress(file);
//            executeDeleteCmd(config.getMediaRoot() + File.separator + file.getInitialData().getRootDir(), config);
//            restClient.deleteFile(file);
//        }
//        catch (FileAlreadyInProgressException e ) {
//            System.out.println("Ignoring file " + file.getId() + ", as it was already taken by another file processor instance.");
//        } catch (RestCallFailedException | FileProcessingFailedException e) {
//            ExecuteTaskJob.handleError(file, restClient, e);
//        }
//    }
//
//    private void executeDeleteCmd(String dir, Config config) throws FileProcessingFailedException {
//        try {
//            CommandLine cmd = CommandLine.parse(config.getDeleteCmd().replace("$dir", dir));
//            System.out.println("Executing " + cmd);
//            new DefaultExecutor().execute(cmd);
//        }
//        catch ( Throwable t ) {
//            throw new FileProcessingFailedException(t);
//        }
//    }
}
