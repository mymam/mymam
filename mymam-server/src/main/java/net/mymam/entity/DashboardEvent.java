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

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author fstab
 */
@NamedQueries({
        @NamedQuery(
                name = "findDashboardEventByUser",
                query = "SELECT e FROM DashboardEvent e WHERE e.user = :user"
        )
})
@Entity
public abstract class DashboardEvent {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private User user;

    //@Past
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date creationDate;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public UploadSuccessfulEvent castToUploadSuccessfulEvent() {
        if ( this instanceof UploadSuccessfulEvent ) {
            return (UploadSuccessfulEvent) this;
        }
        return null;
    }
}
