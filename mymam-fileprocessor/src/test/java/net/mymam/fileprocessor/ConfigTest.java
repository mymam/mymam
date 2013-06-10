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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.quartz.JobDataMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the {@link Config} class.
 *
 * @author fstab
 */
@RunWith(JUnit4.class)
public class ConfigTest {

    private Map<String, String> testData;

    /**
     * Create the unmodifiable testData.
     */
    @Before
    public void setUp() {
        Map<String, String> testData = new HashMap<>();
        testData.put(Config.Var.SERVER_USER.getName(), "test user");
        testData.put(Config.Var.SERVER_PASSWORD.getName(), "test password");
        testData.put(Config.Var.SERVER_URL.getName(), "test url");
        testData.put(Config.Var.CLIENT_CMD_GENERATE_LOWRES_MP4.getName(), "generate mp4");
        testData.put(Config.Var.CLIENT_CMD_GENERATE_LOWRES_WEBM.getName(), "generate webm");
        testData.put(Config.Var.CLIENT_CMD_GENERATE_IMAGE.getName(), "generate image");
        testData.put(Config.Var.CLIENT_CMD_DELETE.getName(), "delete");
        testData.put(Config.Var.CLIENT_MEDIAROOT.getName(), "media root");
        this.testData = Collections.unmodifiableMap(testData);
    }

    /**
     * Compare the contents of map to the contents of testData.
     *
     * @param map the map to be compared.
     */
    private void compareToTestData(Map<? extends Object, ? extends Object> map) {
        for ( Object key : map.keySet() ) {
            assertTrue(key instanceof String);
            assertTrue(testData.containsKey(key));
            assertTrue(testData.get(key).equals(map.get(key)));
        }
        assertEquals(testData.size(), map.size());
    }

    /**
     * Test conversion from {@link Properties} to {@link JobDataMap}.
     */
    @Test
    public void testFromProperties() {
        Properties properties = new Properties();
        for ( String key : testData.keySet() ) {
            properties.put(key, testData.get(key));
        }
        Config config = Config.fromProperties(properties);
        compareToTestData(config.toJobDataMap());
    }

    /**
     * Test creation from {@link JobDataMap}.
     */
    @Test
    public void testFromJobData() {
        JobDataMap jobData = new JobDataMap();
        for ( String key : testData.keySet() ) {
            jobData.put(key, testData.get(key));
        }
        Config config = Config.fromJobDataMap(jobData);
        compareToTestData(config.toJobDataMap());
    }

    /**
     * Test if an {@link IllegalArgumentException} is thrown when
     * {@link Config} is created from incomplete {@link Properties}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testPropertyMissing() {
        Properties properties = new Properties();
        for ( String key : testData.keySet() ) {
            if ( ! key.equals(Config.Var.SERVER_PASSWORD.getName()) ) {
                properties.put(key, testData.get(key));
            }
        }
        Config.fromProperties(properties);
    }
}
