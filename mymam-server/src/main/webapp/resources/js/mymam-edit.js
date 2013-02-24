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

// mymam-edit.js
// ---------

/*global  $, jsf*/
$(function () {

    "use strict";

    function twoDigits(n) {
        return n < 10 ? '0' + n : n;
    }

    function threeDigits(n) {
        if (n < 10) {
            return '00' + n;
        }
        if (n < 100) {
            return '0' + n;
        }
        return n;
    }

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

    // Make the video large on mouse over
    $('#small_video').mouseover(function () {
        var video = $('video'),
            offset = video.offset();
        video.css('position', 'absolute');
        video.css('left', (offset.left - 200) + 'px');
        video.css('top', offset.top + 'px');
        video.css('width', '400px');
        video.mouseout(function () {
            video.css('position', '');
            video.css('left', '');
            video.css('top', '');
            video.css('width', '100%');
        });
    });

    $('#small_video').on('timeupdate', function (evt) {
        var ms = Math.round(evt.target.currentTime * 1000) % 1000,
            sec = Math.floor(evt.target.currentTime) % 60,
            min = Math.floor(evt.target.currentTime / 60) % 60,
            hour = Math.floor(evt.target.currentTime / (60 * 60));
        $('input.thumbframeclass').val(twoDigits(hour) + ':' + twoDigits(min) + ':' + twoDigits(sec) + '.' + threeDigits(ms));
    });
});
