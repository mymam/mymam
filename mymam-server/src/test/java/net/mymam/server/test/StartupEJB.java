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

import net.mymam.ejb.UserMgmtEJB;
import net.mymam.entity.Config;
import net.mymam.entity.Role;
import net.mymam.entity.User;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author fstab
 */
@Startup @Singleton
public class StartupEJB {

    public static final String MEDIA_ROOT = "/tmp/mymam-media-root";

    @EJB
    private UserMgmtEJB userMgmt;

    @PersistenceContext(unitName = "defaultPersistenceUnit")
    private EntityManager em;

    @PostConstruct
    public void initApplication() {
        User adminUser = userMgmt.createUser("admin", "admin");
        Role adminRole = userMgmt.createRole("admin");
        Role userRole = userMgmt.createRole("user");
        userMgmt.addRole(adminUser, adminRole);
        userMgmt.addRole(adminUser, userRole);

        User scriptUser = userMgmt.createUser("system", "system");
        Role scriptRole = userMgmt.createRole("system");
        userMgmt.addRole(scriptUser, scriptRole);

        makeDefaultConfig();
    }

    private void makeDefaultConfig() {
        Config config = new Config();
        config.setMediaRoot(MEDIA_ROOT);
        em.persist(config);
    }
}
