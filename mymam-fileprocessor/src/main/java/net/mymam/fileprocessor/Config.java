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

import org.quartz.JobDataMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author fstab
 */
public class Config {

    public enum Var {
        SERVER_URL("server.url"),
        SERVER_USER("server.user"),
        SERVER_PASSWORD("server.password"),
        CLIENT_MEDIAROOT("client.mediaroot"),
        CLIENT_CMD_GENERATE_LOWRES_MP4("client.cmd.generate_lowres.mp4"),
        CLIENT_CMD_GENERATE_LOWRES_WEBM("client.cmd.generate_lowres.webm"),
        CLIENT_CMD_GENERATE_IMAGE("client.cmd.generate_image"),
        CLIENT_CMD_DELETE("client.cmd.delete");

        private final String name;

        private Var(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private final Map<Var, String> cfg;

    /**
     * Guarantees that cfg contains all {@link Var}s.
     *
     * @param cfg
     */
    private Config(Map<? extends Object, ? extends Object> cfg) {
        Map<Var, String> map = new HashMap<>();
        for ( Var var : Var.values() ) {
            if ( ! cfg.containsKey(var.getName()) ) {
                throw new IllegalArgumentException(var.getName() + " not found in config.");
            }
            map.put(var, cfg.get(var.getName()).toString());
        }
        this.cfg = Collections.unmodifiableMap(map);
    }

    public static Config fromProperties(Properties props) {
        return new Config(props);
    }

    public static Config fromJobDataMap(JobDataMap jobDataMap) {
        return new Config(jobDataMap);
    }

    public JobDataMap toJobDataMap() {
        JobDataMap result = new JobDataMap();
        for ( Var var : cfg.keySet() ) {
            result.put(var.getName(), cfg.get(var));
        }
        return result;
    }

    public String get(Var key) {
        return cfg.get(key);
    }
}
