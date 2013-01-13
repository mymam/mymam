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

import net.mymam.entity.Config;

import javax.ejb.Stateless;
import javax.persistence.*;

/**
 * @author fstab
 */
@Stateless
public class ConfigEJB {

    @PersistenceContext(unitName = "defaultPersistenceUnit")
    private EntityManager em;

    private Config findAttachedConfig() {
        Query query = em.createNamedQuery("findConfig");
        return (Config) query.getSingleResult();
    }

    /**
     * @return a detached {@link Config} object.
     */
    public Config findConfig() {
        Config config = findAttachedConfig();
        em.detach(config);
        return config;
    }

    public void updateMediaRoot(String mediaRoot) {
        // TODO: create media root if it doesn't exist
        Config config = findAttachedConfig();
        config.setMediaRoot(mediaRoot);
    }
}
