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
package net.mymam.ejb;

import net.mymam.entity.DashboardEvent;
import net.mymam.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fstab
 */
public class DashboardEJB {

    @PersistenceContext(unitName = "defaultPersistenceUnit")
    private EntityManager em;

    private List<DashboardEvent> detach(List<DashboardEvent> list) {
        for (DashboardEvent event : list) {
            em.detach(event);
        }
        return list;
    }

    public List<DashboardEvent> findDashboardEventsByUser(User user) {
        Query query = em.createNamedQuery("findDashboardEventByUser").setParameter("user", user);
        List<DashboardEvent> result = query.getResultList();
        if ( result == null ) {
            result = new ArrayList<DashboardEvent>();
        }
        return detach(result);
    }
}
