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
import net.mymam.data.json.FileProcessorTaskResult;
import net.mymam.data.json.FileProcessorTaskType;
import net.mymam.data.json.MediaFile;
import net.mymam.data.json.MediaFileImportStatus;
import net.mymam.fileprocessor.exceptions.ConfigErrorException;
import net.mymam.fileprocessor.exceptions.FileAlreadyInProgressException;
import net.mymam.fileprocessor.exceptions.RestCallFailedException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static net.mymam.data.json.MediaFileImportStatus.*;

/**
 * Provides methods for calling MyMAM's REST services.
 *
 * <p/>
 * The {@link RestClient} should be retrieved from {@link RestClientProvider#getRestClient()}.
 *
 * @author fstab
 */
public class RestClient {

    private final WebResource service;

    /**
     * Package private constructor, because {@link RestClient} should be
     * used via the {@link RestClientProvider}
     *
     * @param server base URL for the REST service, e.g. http://localhost:8080/mymam/rest
     * @param user name of MyMAM's system user.
     * @param password password of MyMAM's system user.
     */
    RestClient(URI server, String user, String password) {
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        Client client = Client.create(clientConfig);
        client.addFilter(new HTTPBasicAuthFilter(user, password));
        this.service= client.resource(server);
    }

    public void ping() throws RestCallFailedException {
        try {
            ClientResponse response = service.path("ping").type(APPLICATION_JSON_TYPE).get(ClientResponse.class);
            if ( response.getStatus() != Response.Status.OK.getStatusCode() ) {
                throw new RestCallFailedException("Ping returned status " + response.getStatus());
            }
        }
        catch ( ClientHandlerException e ) {
            throw new RestCallFailedException("Ping failed: " + service.getURI(), e);
        }
    }

    public MediaFile grabTask(FileProcessorTaskType... taskTypes) throws RestCallFailedException {
        try {
            return service.path("files").path("file-processor-task-reservation").type(APPLICATION_JSON_TYPE).post(MediaFile.class, Arrays.asList(taskTypes));
        }
        catch ( UniformInterfaceException e ) {
            ClientResponse response = e.getResponse();
            if ( response.getStatus() == ClientResponse.Status.NO_CONTENT.getStatusCode() ) {
                return null;
            }
            throw new RestCallFailedException("Failed to grab task from " + service.getURI(), e);
        }
        catch ( ClientHandlerException e ) {
            throw new RestCallFailedException("Failed to grab task from " + service.getURI(), e);
        }
    }

    public void postFileProcessorTaskResult(Long id, FileProcessorTaskResult result) throws RestCallFailedException {
        try {
            service.path("files").path(""+id).path("file-processor-task-result").type(APPLICATION_JSON_TYPE).post(result);
        }
        catch ( UniformInterfaceException | ClientHandlerException e ) {
            throw new RestCallFailedException("Failed to post task result to " + service.getURI(), e);
        }
    }
}
