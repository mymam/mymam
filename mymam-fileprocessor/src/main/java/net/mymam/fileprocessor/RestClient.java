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
import net.mymam.fileprocessor.exceptions.FileAlreadyInProgressException;
import net.mymam.fileprocessor.exceptions.RestCallFailedException;

import javax.ws.rs.core.UriBuilder;
import java.util.Arrays;
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
        catch ( Throwable t ) {
            throw new RestCallFailedException("Failed to grab task from " + service.getURI(), t);
        }
    }

    public void postFileProcessorTaskResult(Long id, FileProcessorTaskResult result) {
        service.path("files").path(""+id).path("file-processor-task-result").type(APPLICATION_JSON_TYPE).post(result);
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
