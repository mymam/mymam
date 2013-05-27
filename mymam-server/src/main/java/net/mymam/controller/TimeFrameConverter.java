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

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.util.concurrent.TimeUnit;

/**
 * @author fstab
 */
@FacesConverter("net.mymam.timeframe")
public class TimeFrameConverter implements Converter {

    @Override
    public Long getAsObject(FacesContext context, UIComponent component, String value) {
        if ( value == null || ! value.matches("^[0-9]*?[0-9]{2}:[0-9]{2}:[0-9]{2}\\.[0-9]{3}$") ) {
            FacesMessage message = new FacesMessage("Conversion error occurred. ", "Invalid time frame value.");
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(message);
        }
        String[] values = value.split(":");
        long hours = Long.parseLong(values[0]);
        long minutes = Long.parseLong(values[1]);
        long seconds = Long.parseLong(values[2].substring(0, 2));
        long millis = Long.parseLong(values[2].substring(3));
        return ( ( hours * 60L + minutes ) * 60L + seconds ) * 1000L + millis;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        long millis = (Long) value;
        return String.format("%02d:%02d:%02d.%03d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60,
                millis % 1000);
    }
}
