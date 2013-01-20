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

package net.mymam.ui;

import org.apache.commons.fileupload.FileItem;

/**
 * Uploaded file, as included in a multipart request.
 *
 * <p/>
 * This is a wrapper class including the {@link FileItem} and the
 * corresponding encoding.
 *
 * @author fstab
 */
public class UploadedFile {

    private final FileItem fileItem;
    private final String encoding;

    public UploadedFile(FileItem fileItem, String encoding) {
        this.fileItem = fileItem;
        this.encoding = encoding;
    }

    /**
     * The encoding is needed for calling {@link FileItem#getString(String)}
     *
     * @return encoding of the file.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * The {@link FileItem}, as included in the multipart request.
     *
     * @return {@link FileItem} from the multipart request.
     */
    public FileItem getFileItem() {
        return fileItem;
    }
}
