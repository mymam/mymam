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
package net.mymam.ui;

import org.primefaces.component.datalist.DataList;

import javax.faces.component.FacesComponent;

/**
 * Same as {@link DataList}, but uses the {@link FloatDivListRenderer}
 * instead of the {@link org.primefaces.component.datalist.DataListRenderer DataListRenderer}.
 *
 * See  http://forum.primefaces.org/viewtopic.php?f=3&t=27885
 *
 * @author fstab
 */
@FacesComponent("net.mymam.ui_component.FloatDivList")
public class FloatDivList extends DataList {

    public FloatDivList() {
        // overwrite renderer
        setRendererType("net.mymam.ui.FloatDivListRenderer");
    }

    @Override
    public String getFamily() {
        return "net.mymam.ui";
    }
}
