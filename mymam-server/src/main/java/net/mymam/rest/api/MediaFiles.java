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
package net.mymam.rest.api;

import net.mymam.data.json.*;
import net.mymam.data.json.MediaFile;
import net.mymam.ejb.MediaFileEJB;
import net.mymam.ejb.UserMgmtEJB;
import net.mymam.entity.*;
import net.mymam.exceptions.NoSuchTaskException;
import net.mymam.exceptions.NotFoundException;
import net.mymam.rest.util.Jpa2Json;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fstab
 */
@Path("/files")
public class MediaFiles {

    @EJB
    private MediaFileEJB mediaFileEJB;

    @EJB
    private UserMgmtEJB userMgmtEJB;

    @POST
    @Path("file-processor-task-reservation")
    @Produces(MediaType.APPLICATION_JSON)
    public MediaFile postFileProcessorTaskReservation(List<FileProcessorTaskType> types) {
        List<Class> classes = new ArrayList<>();
        for ( FileProcessorTaskType type : types ) {
            switch ( type ) {
                case GENERATE_THUMBNAILS:
                    classes.add(GenerateThumbnailImagesTask.class);
                    break;
                case GENERATE_PROXY_VIDEOS:
                    classes.add(GenerateProxyVideosTask.class);
                    break;
                case DELETE:
                    classes.add(DeleteTask.class);
                    break;
                default:
                    throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
        }
        return Jpa2Json.map(mediaFileEJB.grabNextFileProcessorTask(classes));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}/file-processor-task-result")
    public void postFileProcessorTaskResult(@PathParam("id") Long id, FileProcessorTaskResult result) {
        try {
            if ( result.getStatus() == null || result.getTaskType() == null ) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }
            mediaFileEJB.handleTaskResult(id, result.getTaskType(), result.getStatus(), result.getData());
        } catch (NoSuchTaskException e) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        } catch (NotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNewFile(MediaFileInitialData initialData) {
        // TODO: Handle error cases:
        //   * user does not exist
        //   * rootDir or origFile does not exist
        //   * rootDir is already used for another mediaFile
        mediaFileEJB.createNewMediaFile(initialData.getRootDir(), initialData.getOrigFile(), initialData.getUploadingUser());
        return Response.ok().build();
    }
}
