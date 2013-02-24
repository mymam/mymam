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

import net.mymam.data.json.*;
import net.mymam.fileprocessor.exceptions.FileProcessingFailedException;
import net.mymam.fileprocessor.exceptions.FileAlreadyInProgressException;
import net.mymam.fileprocessor.exceptions.RestCallFailedException;
import org.quartz.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fstab
 */
// TODO: Clean up this class!!!
@DisallowConcurrentExecution
public class ExecuteTaskJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            Config config = Config.fromJobDataMap(jobExecutionContext.getMergedJobDataMap());
            RestClient restClient = new RestClient(config);
            VideoFileGenerator fileGen = new VideoFileGenerator(config);
            MediaFile fileWithPendingTask = restClient.grabTask(
                    FileProcessorTaskType.GENERATE_PROXY_VIDEOS,
                    FileProcessorTaskType.GENERATE_THUMBNAILS,
                    FileProcessorTaskType.DELETE
            );
            String rootDir = fileWithPendingTask.getInitialData().getRootDir();
            String origFile = fileWithPendingTask.getInitialData().getOrigFile();
            Map<String, String> resultData;
            switch ( fileWithPendingTask.getNextTask().getTaskType() ) {
                case GENERATE_PROXY_VIDEOS:
                    resultData = fileGen.generateProxyVideos(rootDir, origFile);
                    break;
                case GENERATE_THUMBNAILS:
                    resultData = fileGen.generateThumbnails(rootDir, origFile);
                    break;
                case DELETE:
                    System.out.println("DELETE NOT IMPLEMENTED YET.");
                    resultData = new HashMap<>();
                    break;
                default:
                    resultData = new HashMap<>();
            }
            FileProcessorTaskResult result = new FileProcessorTaskResult();
            result.setTaskType(fileWithPendingTask.getNextTask().getTaskType());
            result.setData(resultData);
            restClient.postFileProcessorTaskResult(fileWithPendingTask.getId(), result);
        } catch (RestCallFailedException e) {
            // TODO: Log as error
            System.out.println("Failed to load file list from server.");
            e.printStackTrace();
        } catch (FileProcessingFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
