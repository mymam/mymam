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

import net.mymam.data.json.MediaFileImportStatus;
import net.mymam.entity.MediaFile;
import net.mymam.exceptions.InvalidInputStatusChangeException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;

/**
 * Helper class for {@link MediaFileStatusTest}.
 *
 * @author fstab
 */
@Stateless
public class MediaFileEJB_ImportStatusWithDelay {

    @PersistenceContext(unitName = "defaultPersistenceUnit")
    private EntityManager em;

    /**
     * Copy-and-paste of {@link net.mymam.ejb.MediaFileEJB#setImportStatusInProgress(long)},
     * but adds a {@link Thread#sleep(long)} between the <i>check-then-act</i> race condition
     * in order to provoke an {@link OptimisticLockException} in {@link MediaFileStatusTest}.
     *
     * @param id ID of the {@link MediaFile} JPA entity.
     * @throws InvalidInputStatusChangeException should be thrown if an internal
     *     {@link OptimisticLockException} has occurred.
     * @throws InterruptedException if {@link Thread#sleep(long)} is interrupted.
     */
    public void setImportStatusInProgressWithDelay(long id) throws InvalidInputStatusChangeException, InterruptedException {
        try {
            MediaFile file = em.find(MediaFile.class, id);
            if ( file.getStatus() != MediaFileImportStatus.NEW ) {
                throw new InvalidInputStatusChangeException(file.getStatus(), MediaFileImportStatus.NEW);
            }
            Thread.sleep(1*1000);
            file.setStatus(MediaFileImportStatus.FILEPROCESSOR_IN_PROGRESS);
            em.flush(); // might throw OptimisticLockException
        }
        catch ( OptimisticLockException e) {
            throw new InvalidInputStatusChangeException(e);
        }
    }
}
