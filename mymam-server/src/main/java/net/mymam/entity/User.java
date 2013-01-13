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
package net.mymam.entity;

import org.jboss.security.auth.spi.Util;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fstab
 */
@NamedQuery(
        name="findUserByName",
        query="SELECT u FROM User u WHERE u.username = :username"
)
@Entity
@Table(name = "USER_TABLE") // User is a reserved word in postgresql
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String username;

    private String hashedPassword;

    @ManyToMany(mappedBy = "users")
    private List<Role> roles;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.hashedPassword = Util.createPasswordHash("MD5", Util.BASE64_ENCODING, null, null, password);
    }

    public List<Role> getRoles() {
        if ( roles == null ) {
            roles = new ArrayList<Role>();
        }
        return roles;
    }
}
