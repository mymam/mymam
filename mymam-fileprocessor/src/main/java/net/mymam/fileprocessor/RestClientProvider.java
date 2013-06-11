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

import net.mymam.fileprocessor.exceptions.ConfigErrorException;
import net.mymam.fileprocessor.exceptions.RestCallFailedException;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import java.io.File;
import java.net.URI;

/**
 * Provides the {@link RestClient} singleton.
 *
 * @author fstab
 */
public class RestClientProvider {

    private static RestClient restClient;

    /**
     * Must be called before the {@link RestClient} singleton is used.
     *
     * <p/>
     * The following configuration checks are performed:
     * <ul>
     *     <li>Check if the media root directory exists.</li>
     *     <li>Ping request to the server in order to verify the server configuration.</li>
     * </ul>
     *
     * @param config configuration, as read from the properties file.
     * @throws ConfigErrorException if a configuration error has been detected.
     * @throws RuntimeException if this method is called multiple times.
     */
    public static void initialize(Config config) throws ConfigErrorException {
        if ( restClient != null ) {
            throw new RuntimeException("RestClientProvider.initialize() called multiple times.");
        }
        if ( ! new File(config.get(Config.Var.CLIENT_MEDIAROOT)).isDirectory() ) {
            throw new ConfigErrorException("Configuration error: Media root directory \"" + config.get(Config.Var.CLIENT_MEDIAROOT) + "\" not found.");
        }
        URI server = stringToUri(config.get(Config.Var.SERVER_URL));
        String user = config.get(Config.Var.SERVER_USER);
        String password = config.get(Config.Var.SERVER_PASSWORD);
        restClient = new RestClient(server, user, password);
        try {
            restClient.ping();
        } catch (RestCallFailedException e) {
            throw new ConfigErrorException("Configuration error: Failed to access REST server \"" + server + "\" with user \"" + user + "\" and password \"" + password + "\"");
        }
    }

    /**
     * Get the {@link RestClient} singleton.
     *
     * @return the {@link RestClient} singleton.
     * @throws RuntimeException if {@link #initialize(Config)} has not been called.
     */
    public static RestClient getRestClient() {
        if ( restClient == null ) {
            throw new RuntimeException("RestClientProvider used without calling initialize().");
        }
        return restClient;
    }

    private static URI stringToUri(String string) throws ConfigErrorException {
        try {
            return UriBuilder.fromUri(string).build();
        }
        catch ( IllegalArgumentException | UriBuilderException e ) {
            throw new ConfigErrorException(string + " is not a valid URI.");
        }
    }
}
