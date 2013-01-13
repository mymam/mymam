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

import net.mymam.ejb.MediaFileEJB;
import net.mymam.entity.MediaFile;
import net.mymam.entity.User;
import net.mymam.exceptions.InvalidInputStatusChangeException;
import net.mymam.exceptions.NotFoundException;

import javax.annotation.security.RunAs;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * Wrapper for calling {@link MediaFileEJB} methods in <i>SYSTEM</i> role.
 *
 * @author fstab
 */
@RunAs("system")
@Stateless
public class MediaFileEJB_AuthWrapper {

    @Inject
    MediaFileEJB mediaFileEJB;

    /**
     * @see {@link MediaFileEJB#createNewMediaFile(String, String, net.mymam.entity.User)}.
     */
    public MediaFile createNewMediaFile(String rootDir, String origFile, User uploadingUser) {
        return mediaFileEJB.createNewMediaFile(rootDir, origFile, uploadingUser);
    }

    /**
     * @see {@link MediaFileEJB#setImportStatusInProgress(long)}
     */
    public void setImportStatusInProgress(long id) throws InvalidInputStatusChangeException, NotFoundException {
        mediaFileEJB.setImportStatusInProgress(id);
    }

    /**
     * @see {@link MediaFileEJB#removeMediaFile(Long)}
     */
    public void removeMediaFile(Long id) {
        mediaFileEJB.removeMediaFile(id);
    }
}
