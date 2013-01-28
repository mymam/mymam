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

// edit.js
// ---------

/*global  $, jsf*/
$(function () {

    "use strict";

    // Small script to style input fields with error messages.
    jsf.ajax.addOnEvent(function () {
        $('div.control-group').each(function (index, div) {
            if ($(div).find('span.help-block').length > 0) {
                if (!$(div).find('span.help-block').is(':empty')) {
                    $(div).addClass('error');
                }
            }
        });
    });
});
