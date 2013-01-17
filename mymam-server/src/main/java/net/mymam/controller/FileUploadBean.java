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
package net.mymam.controller;

import net.mymam.data.json.MediaFileImportStatus;
import net.mymam.ejb.ConfigEJB;
import net.mymam.ejb.MediaFileEJB;
import net.mymam.entity.MediaFile;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author fstab
 */
@ManagedBean
@RequestScoped
public class FileUploadBean implements Serializable {

    @Inject
    private UserBean userBean;

    @EJB
    private ConfigEJB config;

    @EJB
    private MediaFileEJB mediaFileEJB;

    private enum Status {
        SUCCESS,
        FAILED
    };

    private Status status;

    public List<MediaFile> getNewFiles() {
        return mediaFileEJB.findByStatus(MediaFileImportStatus.NEW);
    }

    public List<MediaFile> getFilesForCurrentUser() {
        return mediaFileEJB.findReadyFilesForUser(userBean.getLoggedOnUser());
    }

    public String getDoneLabel() {
        Locale locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
        ResourceBundle messages = ResourceBundle.getBundle("i18n", locale);
        if ( status == Status.SUCCESS ) {
            return messages.getString("upload.done");
        }
        else { // FAILED
            return messages.getString("upload.failed");
        }
    }

    // TODO: Warn user about configuration error when config.findConfig().getMediaRoot() is invalid.
    public void listener(FileUploadEvent event) throws Exception {
        try {
            Path root = makeRootDir();
            Path orig = Paths.get(root.toString(), event.getFile().getFileName());
            moveToPath(event.getFile(), orig);
            String relRoot = Paths.get(config.findConfig().getMediaRoot()).relativize(root).toString();
            String relOrig = root.relativize(orig).toString();
            mediaFileEJB.createNewMediaFile(relRoot, relOrig, userBean.getLoggedOnUser());
            status = Status.SUCCESS;
            // TODO: Test FacesMessage
            FacesMessage msg = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (IOException e) {
            e.printStackTrace();
            status = Status.FAILED;
            // TODO: Test FacesMessage
            FacesMessage msg = new FacesMessage("Failed", event.getFile().getFileName() + " failed.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    private Path makeRootDir() throws IOException {
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd.");
        return Files.createTempDirectory(Paths.get(config.findConfig().getMediaRoot()), myFormat.format(new Date()));
    }

    private Path makePath(String filename) throws IOException {
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd.");
        Path dir = Files.createTempDirectory(Paths.get(config.findConfig().getMediaRoot()), myFormat.format(new Date()));
        return Paths.get(dir.toString(), filename);
    }

    private void moveToPath(UploadedFile file, Path path) throws IOException {
        try (InputStream in = file.getInputstream(); OutputStream out = Files.newOutputStream(path)) {
            int read;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        }
    }
}