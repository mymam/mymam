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

import net.mymam.data.json.FileProcessorTaskStatus;
import net.mymam.entity.FileProcessorTask;
import net.mymam.entity.MediaFile;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

/**
 * @author fstab
 */
@Stateless
public class MediaFileEJB_GrabTaskWithDelay {

    @PersistenceContext(unitName = "defaultPersistenceUnit")
    private EntityManager em;

    /**
     * Copy-and-paste of {@link net.mymam.ejb.MediaFileEJB#grabNextFileProcessorTask(java.util.Collection)},
     * but adds a {@link Thread#sleep(long)} between the <i>check-then-act</i> race condition
     * in order to provoke an {@link javax.persistence.OptimisticLockException} in
     * {@link net.mymam.server.test.GrabTaskTest}.
     *
     * @param types See {@link net.mymam.ejb.MediaFileEJB#grabNextFileProcessorTask(java.util.Collection)}.
     * @throws InterruptedException if {@link Thread#sleep(long)} is interrupted.
     */
    public MediaFile grabNextFileProcessorTask(Collection<Class> types) throws InterruptedException {
        try {
            Query query = em.createNamedQuery("findMediaFileWithPendingTasks")
                    .setParameter("classes", types);
            query.setMaxResults(1);
            List<MediaFile> result = query.getResultList();
            if (result != null && result.size() > 0) {
                MediaFile file = result.get(0);
                FileProcessorTask nextTask = file.getPendingTasksQueue().get(0);
                if ( nextTask.getStatus() != FileProcessorTaskStatus.PENDING ) {
                    throw new IllegalStateException("Query for pending tasks delivered task that is not pending.");
                }
                Thread.sleep(1*1000);
                nextTask.setStatus(FileProcessorTaskStatus.IN_PROGRESS);
                em.persist(nextTask);
                em.flush(); // throws OptimisticLockException
                em.detach(file);
                return file;
            }
            return null;
        }
        catch ( OptimisticLockException e ) {
            return null;
        }
    }
}
