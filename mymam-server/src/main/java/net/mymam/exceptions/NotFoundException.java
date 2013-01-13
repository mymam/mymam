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
package net.mymam.exceptions;

import javax.ejb.ApplicationException;

/**
 * @author fstab
 */
@ApplicationException(rollback = true)
public class NotFoundException extends Exception {
    public NotFoundException(Class clazz, Long id) {
        super("Failed to load enity of class " + clazz.getName() + " with id " + id + ".");
    }
}