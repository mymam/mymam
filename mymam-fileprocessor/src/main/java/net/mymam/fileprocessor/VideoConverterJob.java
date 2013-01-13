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
import net.mymam.data.json.MediaFileGeneratedData;
import net.mymam.fileprocessor.exceptions.FileProcessingFailedException;
import net.mymam.fileprocessor.exceptions.FileAlreadyInProgressException;
import net.mymam.fileprocessor.exceptions.RestCallFailedException;
import org.quartz.*;

import java.util.List;

/**
 * @author fstab
 */
// TODO: Clean up this class!!!
@DisallowConcurrentExecution
public class VideoConverterJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            Config config = Config.fromJobDataMap(jobExecutionContext.getMergedJobDataMap());
            RestClient restClient = new RestClient(config);
            VideoFileGenerator fileGen = new VideoFileGenerator(config);
            List<MediaFile> newFiles = restClient.loadNewFiles();
            for ( MediaFile file : newFiles ) {
                // processSingleFile does not throw an Exception, because
                // errors while processing a single file should not end this loop.
                processSingleFile(file, restClient, fileGen);
            }
        } catch (RestCallFailedException e) {
            // TODO: Log as error
            System.out.println("Failed to load file list from server.");
            e.printStackTrace();
        }
    }

    private void processSingleFile(MediaFile file, RestClient restClient, VideoFileGenerator fileGen) {
        try {
            restClient.setStatusInProgress(file);
            String rootDir = file.getInitialData().getRootDir();
            String origFile = file.getInitialData().getOrigFile();
            MediaFileGeneratedData generatedData = fileGen.generateFiles(rootDir, origFile);
            file.setGeneratedData(generatedData);
            restClient.updateGeneratedData(file);
            restClient.setStatusDone(file);
        }
        catch ( FileAlreadyInProgressException e ) {
            // TODO: Log as info message
            System.out.println("Ignoring file " + file.getId() + ", as it was already taken by another file processor instance.");
        } catch (RestCallFailedException | FileProcessingFailedException e) {
            handleError(file, restClient, e);
        }
    }

    private void handleError(MediaFile file, RestClient restClient, Exception cause) {
        // TODO: Log as error message
        System.out.println("Failed to process file with id " + file.getId());
        cause.printStackTrace();
        try {
            restClient.setStatusFailed(file);
        } catch (RestCallFailedException e) {
            // TODO: Log as error message
            System.out.println("Failed to set error status for file " + file.getId());
        }
    }
}
