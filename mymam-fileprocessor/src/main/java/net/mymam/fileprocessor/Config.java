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

import java.util.Properties;

/**
 * @author fstab
 */
public class Config {

    private String serverUrl;
    private String username;
    private String password;
    private String mediaRoot;
    private String generateLowResMp4Cmd;
    private String generateLowResWebmCmd;
    private String generateImageCmd;

    private Config() {}

    public static Config fromProperties(Properties props) {
        Config config = new Config();
        config.serverUrl = (String) props.get("server.url");
        config.username = (String) props.get("server.user");
        config.password = (String) props.get("server.password");
        config.mediaRoot = (String) props.get("client.mediaroot");
        config.generateLowResMp4Cmd = (String) props.get("client.cmd.generate_lowres.mp4");
        config.generateLowResWebmCmd = (String) props.get("client.cmd.generate_lowres.webm");
        config.generateImageCmd = (String) props.get("client.cmd.generate_image");
        return config;
    }

    public static Config fromJobDataMap(JobDataMap jobDataMap) {
        Config config = new Config();
        config.serverUrl = (String) jobDataMap.get("server.url");
        config.username = (String) jobDataMap.get("server.user");
        config.password = (String) jobDataMap.get("server.password");
        config.mediaRoot = (String) jobDataMap.get("client.mediaroot");
        config.generateLowResMp4Cmd = (String) jobDataMap.get("client.cmd.generate_lowres.mp4");
        config.generateLowResWebmCmd = (String) jobDataMap.get("client.cmd.generate_lowres.webm");
        config.generateImageCmd = (String) jobDataMap.get("client.cmd.generate_image");
        return config;
    }

    public JobDataMap toJobDataMap() {
        JobDataMap result = new JobDataMap();
        result.put("server.url", serverUrl);
        result.put("server.user", username);
        result.put("server.password", password);
        result.put("client.mediaroot", mediaRoot);
        result.put("client.cmd.generate_lowres.mp4", generateLowResMp4Cmd);
        result.put("client.cmd.generate_lowres.webm", generateLowResWebmCmd);
        result.put("client.cmd.generate_image", generateImageCmd);
        return result;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getMediaRoot() {
        return mediaRoot;
    }

    public String getGenerateLowResMp4Cmd() {
        return generateLowResMp4Cmd;
    }

    public String getGenerateLowResWebmCmd() {
        return generateLowResWebmCmd;
    }

    public String getGenerateImageCmd() {
        return generateImageCmd;
    }
}
