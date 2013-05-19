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

package net.mymam.server.test;

import net.mymam.data.json.FileProcessorTaskDataKeys;
import net.mymam.data.json.FileProcessorTaskResult;
import net.mymam.data.json.FileProcessorTaskType;
import net.mymam.data.json.MediaFile;
import net.mymam.fileprocessor.Config;
import net.mymam.fileprocessor.RestClient;
import net.mymam.fileprocessor.exceptions.RestCallFailedException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.*;
import static net.mymam.data.json.FileProcessorTaskDataKeys.*;
import static net.mymam.data.json.FileProcessorTaskType.GENERATE_PROXY_VIDEOS;
import static net.mymam.data.json.FileProcessorTaskType.GENERATE_THUMBNAILS;

/**
 * @author fstab
 */
@RunWith(Arquillian.class)
public class UploadDroneTest {


    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        WebArchive war = Deployments.createFullDeployment();
        // uncomment the following line in order to see a list of deployed files on STDOUT.
        // System.out.println(war.toString(true));
        // uncomment the following line int order to write a copy of the deployment to a file.
        // war.as(ZipExporter.class).exportTo(new File("/tmp/testdeployment.war"), true);
        return war;
    }

    @Drone
    WebDriver driver;

    @ArquillianResource
    URL deploymentUrl;

    private static void login(WebDriver driver) {
        driver.findElement(By.id("navbar:login")).click();
        driver.findElement(By.id("loginform:username")).sendKeys("admin");
        driver.findElement(By.id("loginform:password")).sendKeys("admin");
        driver.findElement(By.id("loginform:submit")).click();
    }

    private static void upload(WebDriver driver, Path path) {
        assertTrue(path.toFile().exists());
        driver.findElement(By.id("navbar:nav-upload-link")).click();
        WebElement fileInputBtn = driver.findElement(By.cssSelector("#fileupload input[type='file']"));
        // <input type="file"> must be visible, otherwise Selenium cannot interact with it.
        // Make opacity > 0 and transform = none in order to mark it as visible.
        ((JavascriptExecutor)driver).executeScript("arguments[0].style.opacity = 1; arguments[0].style.transform = 'none';", fileInputBtn);
        fileInputBtn.sendKeys(path.toAbsolutePath().toString());
        // Submit upload form by clicking on the submit button (default form submit is disabled in mymam-upload.js)
        driver.findElement(By.cssSelector("#fileupload input.submit-button")).click();
    }

    private static Properties makeRestClientProps(URL restURL) {
        Properties props = new Properties();
        props.setProperty("server.url", restURL.toString());
        props.setProperty("server.user", "system");
        props.setProperty("server.password", "system");
        props.setProperty("client.mediaroot", "/tmp");
        props.setProperty("client.cmd.generate_lowres.mp4", "");
        props.setProperty("client.cmd.generate_lowres.webm", "");
        props.setProperty("client.cmd.generate_image", "");
        props.setProperty("client.cmd.delete", "");
        return props;
    }

    private static void runImport(URL restURL) throws RestCallFailedException {
        Config config = Config.fromProperties(makeRestClientProps(restURL));
        RestClient restClient = new RestClient(config);
        for ( int i=0; i<2; i++ ) { // two tasks
            MediaFile mediaFile = restClient.grabTask(GENERATE_PROXY_VIDEOS, GENERATE_THUMBNAILS);
            assertNotNull(mediaFile);
            Map<String, String> resultData = new HashMap<>();
            switch ( mediaFile.getNextTask().getTaskType() ) {
                case GENERATE_PROXY_VIDEOS:
                    resultData.put(LOW_RES_WEMB, "test.webm");
                    resultData.put(LOW_RES_MP4, "test.mp4");
                    break;
                case GENERATE_THUMBNAILS:
                    resultData.put(SMALL_IMG, "small.jpg");
                    resultData.put(MEDIUM_IMG, "medium.jpg");
                    resultData.put(LARGE_IMG, "large.jpg");
                    resultData.put(THUMBNAIL_OFFSET_MS, "0");
                    break;
            }
            FileProcessorTaskResult result = new FileProcessorTaskResult();
            result.setTaskType(mediaFile.getNextTask().getTaskType());
            result.setData(resultData);
            restClient.postFileProcessorTaskResult(mediaFile.getId(), result);
        }
        // test if all tasks are done
        MediaFile mediaFile = restClient.grabTask(GENERATE_PROXY_VIDEOS, GENERATE_THUMBNAILS);
        assertNull(mediaFile);
    }

    /**
     * Anonymous access to upload.xhtml should be redirected
     * to login.xhtml by the {@link net.mymam.security.SecurityFilter}.
     */
    @Test
    public void testUpload() throws RestCallFailedException, MalformedURLException {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.get(deploymentUrl + "index.xhtml");
        login(driver);
        upload(driver, Paths.get("/", "home", "fabian", ".bashrc"));
        driver.findElement(By.id("navbar:nav-dashboard-link")).click();
        assertTrue(driver.findElement(By.id("content")).getText().contains("1 new file is currently being imported."));
        runImport(new URL(deploymentUrl, "rest"));
        // TODO: Click on Dashboard link and make video public
        // TODO: Click on Home link and check if the video is available.
    }
}
