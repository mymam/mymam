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
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main class for running the file processor as a stand-alone Java application.
 *
 * @author fstab
 */
public class Main {

    public static void main(String[] args) {
        try {
            Config config = loadConfig("mymam-fileprocessor.properties");
            RestClientProvider.initialize(config);
            run(config);
        }
        catch ( ConfigErrorException e ) {
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (SchedulerException e) {
            System.err.println("An error occurred when initializing the scheduler: " + e.getMessage());
            System.exit(-1);
        }
    }

    private static Config loadConfig(String filename) throws ConfigErrorException {
        try {
            Properties properties = new Properties();
            InputStream is = Main.class.getClassLoader().getResourceAsStream(filename);
//            InputStream is = new FileInputStream("mymam-fileprocessor/src/main/config/mymam-fileprocessor.properties");
            if ( is == null ) {
                throw new ConfigErrorException(filename + " not found. This file must be in the same directory as the JAR file.");
            }
            properties.load(is);
            return Config.fromProperties(properties);
        }
        catch ( IOException e ) {
            throw new ConfigErrorException(filename + ": An error occurred while reading the file: " + e.getMessage());
        }
    }

    private static void run(Config config) throws SchedulerException {

        JobDetail executeTaskJob = JobBuilder.newJob(ExecuteTaskJob.class)
                .withIdentity("execute task job", Scheduler.DEFAULT_GROUP)
                .usingJobData(config.toJobDataMap())
                .build();

        Trigger executeTaskTrigger = TriggerBuilder
                .newTrigger()
                .withIdentity("execute task trigger", Scheduler.DEFAULT_GROUP)
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                .build();

        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        scheduler.scheduleJob(executeTaskJob, executeTaskTrigger);
    }
}
