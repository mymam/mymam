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

import net.mymam.data.json.MediaFileGeneratedData;
import net.mymam.fileprocessor.exceptions.FileProcessingFailedException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author fstab
 */
public class VideoFileGenerator {

    private final Config config;

    public VideoFileGenerator(Config config) {
        this.config = config;
    }

    public MediaFileGeneratedData generateFiles(String rootDir, String origFile) throws FileProcessingFailedException {
        Path origPath = getOrigPath(rootDir, origFile);
        Path generatedDir = makeGeneratedDir(origPath);

        Path mp4 = generateFile(config.getGenerateLowResMp4Cmd(), origPath, generatedDir, "lowRes.mp4");
        Path webm = generateFile(config.getGenerateLowResWebmCmd(), origPath, generatedDir, "lowRes.webm");
        Path large = generateFile(config.getGenerateLargeImageCmd(), origPath, generatedDir, "large.jpg");
        Path small = generateFile(config.getGenerateSmallImageCmd(), origPath, generatedDir, "small.jpg");

        MediaFileGeneratedData result = new MediaFileGeneratedData();
        result.setLowResMp4(relPath(mp4, rootDir));
        result.setLowResWebm(relPath(webm, rootDir));
        result.setPreviewImg(relPath(large, rootDir));
        result.setThumbnail(relPath(small, rootDir));

        return result;
    }

    private Path getOrigPath(String rootDir, String origFile) throws FileProcessingFailedException {
        Path origPath = Paths.get(config.getMediaRoot(), rootDir, origFile);
        if ( ! Files.isRegularFile(origPath) ) {
            throw new FileProcessingFailedException(origFile + " not found.");
        }
        return origPath;
    }

    private Path makeGeneratedDir(Path origPath) throws FileProcessingFailedException {
        try {
            Path generatedDir = Paths.get(origPath.getParent().toString(), "generated");
            // createDirectory throws an exception if generatedDir already exists.
            Files.createDirectory(generatedDir);
            return generatedDir;
        }
        catch ( Throwable t ) {
            throw new FileProcessingFailedException(t);
        }
    }

    private Path generateFile(String cmdLineTemplate, Path in, Path outDir, String outFile) throws FileProcessingFailedException {
        try {
            Path out = Paths.get(outDir.toString(), outFile);
            // TODO: Make sure that weired file names cannot be used to inject shell scripts, like '"; rm -r *;'
            String cmdLine = cmdLineTemplate
                    .replace("$in", "\"" + in.toString() + "\"")
                    .replace("$out", "\"" + out.toString() + "\"");
            CommandLine cmd = CommandLine.parse(cmdLine);
            System.out.println("Executing " + cmd); // TODO: Use logging.
            new DefaultExecutor().execute(cmd);
            return out;
        }
        catch ( Throwable t ) {
            throw new FileProcessingFailedException(t);
        }
    }

    private String relPath(Path fullPath, String rootDir) {
        // throws InvalidPathException, IllegalArgumentException
        return Paths.get(config.getMediaRoot(), rootDir).relativize(fullPath).toString();
    }
}