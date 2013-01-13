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

import net.mymam.ejb.ConfigEJB;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.component.html.HtmlInputText;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author fstab
 */
@Named
@RequestScoped
public class AdminBean {

    @Inject
    private ConfigEJB config;

    private HtmlInputText inputComponent = new HtmlInputText();

    @PostConstruct
    public void init() {
        inputComponent.setValue(config.findConfig().getMediaRoot());
    }

    public void updateMediaRoot(String mediaRoot) {
        config.updateMediaRoot(mediaRoot);
    }

    public HtmlInputText getInputComponent() {
        return inputComponent;
    }
}
