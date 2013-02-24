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

import net.mymam.ejb.ConfigEJB;
import net.mymam.ejb.MediaFileEJB;
import net.mymam.entity.MediaFile;
import net.mymam.ui.UploadedFile;

import javax.ejb.EJB;
import javax.faces.FacesException;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Backing bean for the {@link net.mymam.ui.Upload Upload} component.
 *
 * @author fstab
 */
@ManagedBean
@RequestScoped
public class FileUploadBean implements Serializable {

    @ManagedProperty(value = "#{userBean}")
    private UserBean userBean;

    @EJB
    private ConfigEJB config;

    @EJB
    private MediaFileEJB mediaFileEJB;

    private UploadedFile uploadedFile;

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

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    /**
     * Save the uploaded file data into a new folder in media root,
     * and create a new file entity using the {@link MediaFileEJB}.
     *
     * @param uploadedFile points to the file data in the HTTP request body.
     */
    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
        process(uploadedFile);
    }

    private void process(UploadedFile file) {
        try {
            if ( ! userBean.isLoggedOn() ) {
                // This should not happen, as the upload page is restricted to authenticated users.
                err("Anonymous uploads not supported.");
                return;
            }
            makeMediaRoot();
            Path root = makeRootDir();
            Path orig = Paths.get(root.toString(), file.getFileItem().getName());
            moveToPath(file, orig);
            String relRoot = Paths.get(config.findConfig().getMediaRoot()).relativize(root).toString();
            String relOrig = root.relativize(orig).toString();
            mediaFileEJB.createNewMediaFile(relRoot, relOrig, userBean.getLoggedOnUser());
        } catch (IOException e) {
            err("Failed to save " + file.getFileItem().getName() + ": " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * The file upload requests are not standard JSF requests: they are triggered
     * in a generated iframe using jQuery's file upload plugin.
     *
     * <p/>
     * Therefore, error handling is limited: Faces messages will not be handled
     * correctly. The only way to report an error to the client is to send an HTTP error code.
     * 
     * @param msg is used for logging and may be seen in the browser's debugger,
     *            but will not be displayed in the HTML page.
     */
    private void err(String msg) {
        try {
            System.err.println(msg);
            FacesContext.getCurrentInstance().getExternalContext().responseSendError(500, msg);
            FacesContext.getCurrentInstance().responseComplete();
        }
        catch ( IOException e ) {
            throw new FacesException(e);
        }
    }

    private void makeMediaRoot() throws IOException {
        File mediaRoot = Paths.get(config.findConfig().getMediaRoot()).toFile();
        if ( ! mediaRoot.exists() ) {
            if ( ! mediaRoot.mkdirs() ) {
                throw new IOException("Cannot create " + config.findConfig().getMediaRoot());
            }
        }
    }

    private Path makeRootDir() throws IOException {
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd.");
        return Files.createTempDirectory(Paths.get(config.findConfig().getMediaRoot()), myFormat.format(new Date()));
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