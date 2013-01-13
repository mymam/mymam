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

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author fstab
 */
public class Main {

    public static final String PROPERTIES_FILE = "mymam-fileprocessor.properties";

    private static Properties readProperties() throws IOException {
        Properties properties = new Properties();
        InputStream is = Main.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        if ( is == null ) {
            System.err.println(PROPERTIES_FILE + " not found. This file must be in the same directory as the JAR file.");
            System.exit(-1);
        }
        properties.load(is);
        return properties;
    }

    public static void main(String[] args) throws SchedulerException, IOException {

        Properties properties = readProperties();
        Config config = Config.fromProperties(properties);

        JobDetail job = JobBuilder.newJob(VideoConverterJob.class)
                .withIdentity("video converter job", "group1")
                .usingJobData(config.toJobDataMap())
                .build();

        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("video converter trigger", "group1")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                .build();

        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        scheduler.scheduleJob(job, trigger);
    }
}
