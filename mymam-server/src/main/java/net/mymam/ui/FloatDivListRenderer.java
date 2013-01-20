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
import org.primefaces.component.datalist.DataListRenderer;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import java.io.IOException;

/**
 * Extends {@link DataListRenderer}, but adds a
 * <pre>
 *     &lt;div class="clear"/&gt;&lt;/div&gt;
 * </pre>
 * at the end of the list.
 *
 * See http://forum.primefaces.org/viewtopic.php?f=3&t=27885
 *
 * @author fstab
 */
@FacesRenderer(
        componentFamily = "net.mymam.ui", // must be the same as FloatDivList.getFamily()
        rendererType = "net.mymam.ui.FloatDivListRenderer"
)
public class FloatDivListRenderer extends DataListRenderer {

    private enum PropertyKeys {

        addFilesLabel,
    };

        @Override
    protected void encodeFreeList(FacesContext context, DataList list) throws IOException {
        super.encodeFreeList(context, list);
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("div", null);
        writer.writeAttribute("class", "clear", null);
        writer.endElement("div");
    }


}
