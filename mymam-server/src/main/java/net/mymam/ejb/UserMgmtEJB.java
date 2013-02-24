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

import net.mymam.entity.Role;
import net.mymam.entity.User;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * @author fstab
 */
@Stateless
public class UserMgmtEJB {

    @PersistenceContext(unitName = "defaultPersistenceUnit")
    private EntityManager em;

    public User findUserByName(String username) {
        Query query = em.createNamedQuery("findUserByName").setParameter("username", username);
        User user = (User) query.getSingleResult();
        em.flush(); // must flush before detatch
        em.detach(user);
        return user;
    }

    public Role findRoleByName(String name) {
        Query query = em.createNamedQuery("findRoleByName").setParameter("name", name);
        Role role = (Role) query.getSingleResult();
        em.flush(); // must flush before detatch
        em.detach(role);
        return role;
    }

    public User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        em.persist(user);
        em.flush(); // must flush before detatch
        em.detach(user);
        return user;
    }

    public Role createRole(String name) {
        Role role = new Role();
        role.setName(name);
        em.persist(role);
        em.flush(); // must flush before detatch
        em.detach(role);
        return role;
    }

    public void addRole(User user, Role role) {
        // Perform changes on detached objects.
        user.getRoles().add(role);
        role.getUsers().add(user);
        // Perform changes on managed objects.
        // Don't merge here, because we don't want to persist any other changes here.
        user = em.find(User.class, user.getId());
        role = em.find(Role.class, role.getId());
        user.getRoles().add(role);
        role.getUsers().add(user);
        em.flush(); // must flush before detatch
        em.detach(user);
        em.detach(role);
    }

    // TODO: Exception if there are still MediaFiles referencing this user.
    public void removeUser(User user) {
        user = em.find(User.class, user.getId());
        for ( Role role : user.getRoles() ) {
            role.getUsers().remove(user);
        }
        em.remove(user);
    }

    // TODO: Rename this method, so that it cannot be understood as the counterpart of addRole()
    public void removeRole(Role role) {
        em.remove(em.find(Role.class, role.getId()));
    }
}
