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

import net.mymam.data.json.MediaFile;
import net.mymam.data.json.MediaFileGeneratedData;
import net.mymam.data.json.MediaFileImportStatus;
import net.mymam.data.json.MediaFileInitialData;
import net.mymam.ejb.MediaFileEJB;
import net.mymam.ejb.UserMgmtEJB;
import net.mymam.entity.User;
import net.mymam.exceptions.InvalidImportStateException;
import net.mymam.exceptions.InvalidInputStatusChangeException;
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<MediaFile> getFiles(@QueryParam("status") MediaFileImportStatus status) {
        List<MediaFile> result = new ArrayList<>();
        for ( net.mymam.entity.MediaFile jpaEntity : mediaFileEJB.findByStatus(status) ) {
            result.add(Jpa2Json.map(jpaEntity));
        }
        return result;
    }

    @PUT
    @Path("{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateStatus(@PathParam("id") Long id, MediaFileImportStatus newStatus) {
        try {
            net.mymam.entity.MediaFile jpaEntity = mediaFileEJB.findById(id);
            if ( jpaEntity.getStatus() == MediaFileImportStatus.NEW && newStatus == MediaFileImportStatus.FILEPROCESSOR_IN_PROGRESS) {
                mediaFileEJB.setImportStatusInProgress(id);
            }
            else if ( jpaEntity.getStatus() == MediaFileImportStatus.FILEPROCESSOR_IN_PROGRESS && newStatus == MediaFileImportStatus.FILEPROCESSOR_FAILED ) {
                mediaFileEJB.setImportStatusFailed(id);
            }
            else if ( jpaEntity.getStatus() == MediaFileImportStatus.FILEPROCESSOR_IN_PROGRESS && newStatus == MediaFileImportStatus.FILEPROCESSOR_DONE ) {
                mediaFileEJB.setImportStatusDone(id);
            }
            else if ( jpaEntity.getStatus() == MediaFileImportStatus.MARKED_FOR_DELETION && newStatus == MediaFileImportStatus.DELETION_IN_PROGRESS ) {
                mediaFileEJB.setImportStatusDeletionInProgress(id);
            }
            else {
                return Response.status(Response.Status.CONFLICT).build();
            }
            return Response.ok().build();
        }
        catch ( NotFoundException e ) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        catch ( InvalidInputStatusChangeException e ) {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    @PUT
    @Path("{id}/generated-data")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateGeneratedData(@PathParam("id") Long id, MediaFileGeneratedData jsonEntity) {
        try {
            mediaFileEJB.updateGeneratedData(id, Jpa2Json.map(jsonEntity));
            return Response.ok().build();
        }
        catch ( NotFoundException e ) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (InvalidImportStateException e) {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postNewFile(MediaFileInitialData initialData) {
        // TODO: Handle error cases:
        //   * user does not exist
        //   * rootDir or origFile does not exist
        //   * rootDir is already used for another mediaFile
        User user = userMgmtEJB.findUserByName(initialData.getUploadingUser());
        mediaFileEJB.createNewMediaFile(initialData.getRootDir(), initialData.getOrigFile(), user);
        return Response.ok().build();
    }

    @DELETE
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteFile(@PathParam("id") long id) {
        try {
            mediaFileEJB.deleteFile(id);
            return Response.ok().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (InvalidImportStateException e) {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }
}
