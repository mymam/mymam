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
package net.mymam.test.manual;

import net.mymam.data.json.MediaFile;
import net.mymam.fileprocessor.Config;
import net.mymam.fileprocessor.RestClient;
import net.mymam.fileprocessor.exceptions.FileAlreadyInProgressException;
import net.mymam.fileprocessor.exceptions.RestCallFailedException;

import java.util.Properties;

/**
 * @author fstab
 */
public class ConcurrentProcessingTestManual {

    public static void main(String[] args) throws FileAlreadyInProgressException, RestCallFailedException {
        ConcurrentProcessingTestManual test = new ConcurrentProcessingTestManual();
        test.run();
    }

    public void run() throws RestCallFailedException, FileAlreadyInProgressException {

        Config config = makeTestConfig();

        RestClient client1 = new RestClient(config);
        RestClient client2 = new RestClient(config);

        // One instance of file processor gets a media file.
        MediaFile instance1 = client1.loadNewFiles().get(0);

        // Another instance of file processor gets the same media file.
        MediaFile instance2 = client2.loadNewFiles().get(0);

        // The second instance takes it.
        client2.setStatusInProgress(instance2);

        // The first instance gets an error when attempting to take it.
        client1.setStatusInProgress(instance1); // TODO: Expect FileAlreadyInProgressException
    }

    private Config makeTestConfig() {
        // TODO: Use Mockito to create test config.
        Properties props = new Properties();
        props.setProperty("server.url", "http://localhost:8080/mymam-server-0.1/rest");
        props.setProperty("server.user", "system");
        props.setProperty("server.password", "system");
        return Config.fromProperties(props);
    }
}
