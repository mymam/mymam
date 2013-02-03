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

// mymam-nav.js
// ---------
// Small script to mark the current page in the navigation bar as _active_.
//
// @author fstab

/*global  $*/
$(function () {

    "use strict";

    var path2id = {
        'index.xhtml': '#nav-home',
        'dashboard.xhtml': '#nav-dashboard',
        'projects.xhtml': '#nav-projects',
        'upload.xhtml': '#nav-upload',
        'admin.xhtml': '#nav-admin'
    };

    $.each(path2id, function (path, id) {
        if (window.location.pathname.indexOf(path) !== -1) {
            $(id).addClass('active');
        } else {
            $(id).removeClass('active');
        }
    });
});
