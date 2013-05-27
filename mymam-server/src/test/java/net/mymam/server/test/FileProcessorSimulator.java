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

import net.mymam.data.json.FileProcessorTaskResult;
import net.mymam.data.json.MediaFile;
import net.mymam.fileprocessor.Config;
import net.mymam.fileprocessor.RestClient;
import net.mymam.fileprocessor.exceptions.RestCallFailedException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static junit.framework.Assert.assertNull;
import static net.mymam.data.json.FileProcessorTaskDataKeys.*;
import static net.mymam.data.json.FileProcessorTaskType.GENERATE_PROXY_VIDEOS;
import static net.mymam.data.json.FileProcessorTaskType.GENERATE_THUMBNAILS;

/**
 * @author fstab
 */
public class FileProcessorSimulator {

    public static void runImport(URL restURL) throws RestCallFailedException, IOException {
        Config config = Config.fromProperties(makeRestClientProps(restURL));
        RestClient restClient = new RestClient(config);
        runImport(restClient);
    }

    private static void runImport(RestClient restClient) throws RestCallFailedException, IOException {
        for ( int i=0; i<2; i++ ) { // two tasks
            MediaFile mediaFile = restClient.grabTask(GENERATE_PROXY_VIDEOS, GENERATE_THUMBNAILS);
            Path rootDir = Paths.get(StartupEJB.MEDIA_ROOT, mediaFile.getInitialData().getRootDir());
            Path generatedDir = makeGeneratedDir(rootDir);
            Map<String, String> resultData = new HashMap<>();
            switch ( mediaFile.getNextTask().getTaskType() ) {
                case GENERATE_PROXY_VIDEOS:
                    resultData.put(LOW_RES_WEMB, rootDir.relativize(writeVideoFile(generatedDir.resolve("test.webm"))).toString());
                    resultData.put(LOW_RES_MP4, rootDir.relativize(writeVideoFile(generatedDir.resolve("test.mp4"))).toString());
                    break;
                case GENERATE_THUMBNAILS:
                    resultData.put(SMALL_IMG, rootDir.relativize(FileProcessorSimulator.writeJpg(generatedDir.resolve("small.jpg"), 100, 75)).toString());
                    resultData.put(MEDIUM_IMG, rootDir.relativize(FileProcessorSimulator.writeJpg(generatedDir.resolve("medium.jpg"), 200, 150)).toString());
                    resultData.put(LARGE_IMG, rootDir.relativize(FileProcessorSimulator.writeJpg(generatedDir.resolve("large.jpg"), 400, 300)).toString());
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

    private static Path makeGeneratedDir(Path rootDir) throws IOException {
        Path generatedDir = rootDir.resolve("generated");
        if ( ! Files.exists(generatedDir) ) {
            Files.createDirectory(generatedDir);
        }
        return generatedDir;
    }

    private static Properties makeRestClientProps(URL restURL) {
        Properties props = new Properties();
        props.setProperty("server.url", restURL.toString());
        props.setProperty("server.user", "system");
        props.setProperty("server.password", "system");
        props.setProperty("client.mediaroot", StartupEJB.MEDIA_ROOT);
        props.setProperty("client.cmd.generate_lowres.mp4", "");
        props.setProperty("client.cmd.generate_lowres.webm", "");
        props.setProperty("client.cmd.generate_image", "");
        props.setProperty("client.cmd.delete", "");
        return props;
    }

    /**
     * Writes an invalid video file with dummy data.
     */
    private static Path writeVideoFile(Path path) throws IOException {
        try ( OutputStream stream = Files.newOutputStream(path) ) {
            stream.write("Dummy video data.".getBytes());
            return path;
        }
    }

    /**
     * Writes a valid JPG file with "test" displayed in white on black background.
     */
    private static Path writeJpg(Path path, int width, int height) throws IOException {
        String drawString = "test";
        int fontSize = height / 2;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setFont(new Font("Arial", Font.BOLD, fontSize));
        graphics.drawString(drawString, (width - graphics.getFontMetrics().stringWidth(drawString)) / 2, (height + fontSize)/2);
        graphics.dispose();

        ImageIO.write(image, "jpg", path.toFile());
        return path;
    }
}
