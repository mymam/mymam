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
package net.mymam.controller;

import net.mymam.ejb.DashboardEJB;
import net.mymam.entity.DashboardEvent;
import net.mymam.entity.User;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author fstab
 */
@Named
@RequestScoped
public class DashboardBean {

    @Inject
    private DashboardEJB dashboard;

    @Inject
    private UserBean userBean;

    public List<DashboardEvent> getDashboardEvents() {
        User user = userBean.getLoggedOnUser();
        return dashboard.findDashboardEventsByUser(user);
    }

    public UIComponent getUIComponent() {
        return null; // TODO
    }
}
