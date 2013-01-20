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
import net.mymam.ui.UploadedFile;

import javax.ejb.EJB;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
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

/**
 * @author fstab
 */
@ManagedBean
@RequestScoped
public class FileUploadBootstrapBean implements Serializable {

    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    @EJB
    private ConfigEJB config;

    @EJB
    private MediaFileEJB mediaFileEJB;

    private UploadedFile uploadedFile;

    private enum Status {
        SUCCESS,
        FAILED
    };

    private Status status;

    /**
     * Must provide getter for {@link ManagedProperty}.
     *
     * @return userBean to find the logged-on user who uploaded the file.
     */
    public UserBean getUserBean() {
        return userBean;
    }

    /**
     * Must provide setter for {@link ManagedProperty}.
     *
     * @param userBean to find the logged-on user who uploaded the file.
     */
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public List<MediaFile> getNewFiles() {
        return mediaFileEJB.findByStatus(MediaFileImportStatus.NEW);
    }

    public List<MediaFile> getFilesForCurrentUser() {
        return mediaFileEJB.findReadyFilesForUser(userBean.getLoggedOnUser());
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    // TODO: Warn user about configuration error when config.findConfig().getMediaRoot() is invalid.
    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
        process(uploadedFile);
    }

    private void process(UploadedFile file) {
        try {
            if ( ! userBean.isLoggedOn() ) {
                // This should not happen, as the upload page is restricted to authenticated users.
                throw new FacesException("Anonymous uploads not supported.");
            }
            Path root = makeRootDir();
            Path orig = Paths.get(root.toString(), file.getFileItem().getName());
            moveToPath(file, orig);
            String relRoot = Paths.get(config.findConfig().getMediaRoot()).relativize(root).toString();
            String relOrig = root.relativize(orig).toString();
            mediaFileEJB.createNewMediaFile(relRoot, relOrig, userBean.getLoggedOnUser());
            status = Status.SUCCESS;
            // TODO: Test FacesMessage
            FacesMessage msg = new FacesMessage("Succesful", file.getFileItem().getName() + " is uploaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);
        } catch (IOException e) {
            e.printStackTrace();
            status = Status.FAILED;
            // TODO: Test FacesMessage
            FacesMessage msg = new FacesMessage("Failed", file.getFileItem().getName() + " failed.");
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
        try (InputStream in = file.getFileItem().getInputStream(); OutputStream out = Files.newOutputStream(path)) {
            int read;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        }
    }
}