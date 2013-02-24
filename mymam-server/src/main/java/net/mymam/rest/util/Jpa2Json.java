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
package net.mymam.rest.util;

import net.mymam.data.json.*;
import net.mymam.entity.*;
import net.mymam.entity.FileProcessorTask;
import net.mymam.entity.MediaFile;

import java.util.HashMap;
import java.util.Map;

// Why don't I just use the JPA entities in the REST interface?
//    * mymam-fileprocessor should not have a dependency to javax.persistence
//    * The entities are slightly different
//        - JPA-Entity has ref to User, while JSON entity just stores username.
//        - JSON-Entity has MediaFileInitialData, which does not exactly match the JPA model
/**
 * @author fstab
 */
public class Jpa2Json {

    private static MediaFileInitialData extractInitialData(MediaFile jpaEntity) {
        MediaFileInitialData jsonEntity = new MediaFileInitialData();
        jsonEntity.setOrigFile(jpaEntity.getOrigFile());
        jsonEntity.setRootDir(jpaEntity.getRootDir());
        jsonEntity.setUploadingUser(jpaEntity.getUploadingUser().getUsername());
        return jsonEntity;
    }

    public static net.mymam.data.json.MediaFile map(MediaFile jpaEntity) {
        if ( jpaEntity == null ) {
            return null;
        }
        net.mymam.data.json.MediaFile jsonEntity = new net.mymam.data.json.MediaFile();
        jsonEntity.setCreationDate(jpaEntity.getCreationDate());
        jsonEntity.setId(jpaEntity.getId());
        jsonEntity.setStatus(jpaEntity.getStatus());
        jsonEntity.setInitialData(extractInitialData(jpaEntity));
        if ( jpaEntity.getPendingTasksQueue().size() > 0 ) {
            jsonEntity.setNextTask(map(jpaEntity.getPendingTasksQueue().get(0)));
        }
        return jsonEntity;
    }

    public static net.mymam.data.json.FileProcessorTask map(FileProcessorTask jpaEntity) {
        net.mymam.data.json.FileProcessorTask jsonEntity = new net.mymam.data.json.FileProcessorTask();
        Map<String, String> params = new HashMap<>();
        if ( jpaEntity instanceof GenerateThumbnailImagesTask ) {
            jsonEntity.setTaskType(FileProcessorTaskType.GENERATE_THUMBNAILS);
            params.put(FileProcessorTaskDataKeys.THUMBNAIL_OFFSET_MS, ((GenerateThumbnailImagesTask) jpaEntity).getThumbnailOffsetMs().toString());
        }
        else if ( jpaEntity instanceof GenerateProxyVideosTask ) {
            jsonEntity.setTaskType(FileProcessorTaskType.GENERATE_PROXY_VIDEOS);
        }
        else {
            throw new IllegalArgumentException("jpaEntity must be instance of " + GenerateThumbnailImagesTask.class.getName());
        }
        jsonEntity.setParameters(params);
        return jsonEntity;
    }
}
