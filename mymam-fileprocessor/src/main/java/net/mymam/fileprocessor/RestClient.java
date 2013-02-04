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

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import net.mymam.data.json.MediaFile;
import net.mymam.data.json.MediaFileImportStatus;
import net.mymam.fileprocessor.exceptions.FileAlreadyInProgressException;
import net.mymam.fileprocessor.exceptions.RestCallFailedException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static net.mymam.data.json.MediaFileImportStatus.*;

/**
 * @author fstab
 */
public class RestClient {

    private final WebResource service;

    public RestClient(Config config) throws RestCallFailedException {
        service = makeWebResource(config);
    }

    public List<MediaFile> loadNewFiles() throws RestCallFailedException {
        return loadByStatus(NEW);
    }

    public List<MediaFile> loadFilesMarkedForDeletion() throws RestCallFailedException {
        return loadByStatus(MARKED_FOR_DELETION);
    }

    private List<MediaFile> loadByStatus(MediaFileImportStatus status) throws RestCallFailedException {
        try {
            return service.path("files").queryParam("status", status.toString()).get(new GenericType<List<MediaFile>>() {
            });
        }
        catch ( Throwable t ) {
            throw new RestCallFailedException("Failed to get file list from " + service.getURI(), t);
        }
    }

    public void updateGeneratedData(MediaFile file) throws RestCallFailedException {
        try {
            service.path("files").path(file.getId().toString()).path("generated-data").type(APPLICATION_JSON_TYPE).put(file.getGeneratedData());
        }
        catch ( Throwable t ) {
            throw new RestCallFailedException("Failed to update path info for media file " + file.getId(), t);
        }
    }

    public void setDeletionInProgress(MediaFile file) throws FileAlreadyInProgressException, RestCallFailedException {
        try {
            setStatus(file, DELETION_IN_PROGRESS);
        }
        catch ( UniformInterfaceException e ) {
            if ( e.getResponse().getStatus() == ClientResponse.Status.CONFLICT.getStatusCode()) {
                throw new FileAlreadyInProgressException();
            }
            else {
                throw new RestCallFailedException("Failed to set status " + FILEPROCESSOR_IN_PROGRESS + " for media file " + file.getId() + ".", e);
            }
        }
        catch ( Throwable t ) {
            throw new RestCallFailedException("Failed to set status " + FILEPROCESSOR_IN_PROGRESS + " for media file " + file.getId() + ".", t);
        }
    }

    public void deleteFile(MediaFile file) throws RestCallFailedException {
        try {
            service.path("files").path(file.getId().toString()).type(APPLICATION_JSON_TYPE).delete();
        }
        catch ( Throwable t ) {
            throw new RestCallFailedException("Failed to delete media file " + file.getId() + ".", t);
        }
    }

    public void setStatusInProgress(MediaFile file) throws FileAlreadyInProgressException, RestCallFailedException {
        try {
            setStatus(file, FILEPROCESSOR_IN_PROGRESS);
        }
        catch ( UniformInterfaceException e ) {
            if ( e.getResponse().getStatus() == ClientResponse.Status.CONFLICT.getStatusCode()) {
                throw new FileAlreadyInProgressException();
            }
            else {
                throw new RestCallFailedException("Failed to set status " + FILEPROCESSOR_IN_PROGRESS + " for media file " + file.getId() + ".", e);
            }
        }
        catch ( Throwable t ) {
            throw new RestCallFailedException("Failed to set status " + FILEPROCESSOR_IN_PROGRESS + " for media file " + file.getId() + ".", t);
        }
    }

    public void setStatusDone(MediaFile file) throws RestCallFailedException {
        try {
            setStatus(file, MediaFileImportStatus.FILEPROCESSOR_DONE);
        }
        catch ( Throwable t ) {
            throw new RestCallFailedException("Failed to set status " + MediaFileImportStatus.FILEPROCESSOR_DONE +  " for media file " + file.getId(), t);
        }
    }

    public void setStatusFailed(MediaFile file) throws RestCallFailedException {
        try {
            setStatus(file, MediaFileImportStatus.FILEPROCESSOR_FAILED);
        }
        catch ( Throwable t ) {
            throw new RestCallFailedException("Failed to set status " + MediaFileImportStatus.FILEPROCESSOR_FAILED +  " for media file " + file.getId(), t);
        }
    }

    private void setStatus(MediaFile file, MediaFileImportStatus status) throws FileAlreadyInProgressException, RestCallFailedException {
        service.path("files").path(file.getId().toString()).path("status").type(APPLICATION_JSON_TYPE).put(status);
    }

    private WebResource makeWebResource(Config config) throws RestCallFailedException {
        try {
            ClientConfig clientConfig = new DefaultClientConfig();
            clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
            Client client = Client.create(clientConfig);
            client.addFilter(new HTTPBasicAuthFilter(config.getUsername(), config.getPassword()));
            return client.resource(UriBuilder.fromUri(config.getServerUrl()).build());
        }
        catch ( Throwable t ) {
            throw new RestCallFailedException("Failed to create web resource from configuration.", t);
        }
    }
}
