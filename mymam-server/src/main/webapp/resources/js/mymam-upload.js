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

// mymam-upload.js
// ---------
// This JavaScript implements the user interface for
// MyMAM's multiple file upload.
// It is based on the jQuery file upload plugin,
// https://github.com/blueimp/jQuery-File-Upload
//
// The script is a replacement for jquery.fileupload-ui.js
// in the plugin's original GUI.
//
// @author fstab

/*global  $*/
$(function () {

    "use strict";

    var dataSelections = [],            // Each data selection contains one file to be uploaded.
        nDone = 0,                      // Number of successfully finished uploads.
        nFailed = 0,                    // Number of failed uploads.
        started = false,                // Becomes true with the first send event.
        $fileupload = $('#fileupload'), // The entire fileupload is inside a <form id="fileupload">
        progressMsgTemplate,            // Template for displaying the progress message during upload.

        // initialization
        init,

        // callbacks for jQuery's file upload plugin
        cb_add,
        cb_send,
        cb_progress,
        cb_progressall,
        cb_done,
        cb_failed,

        // helper functions
        createTableRow,
        removeTableRow,
        makeSelectButtonActive,
        makeSubmitButtonActive,
        updateLineNumbers,
        formatSize,
        twoDigit,
        startUpload,
        endUpload;

    // init()
    // ------
    // Initialize the jQuery file upload plugin, and enable the 'Submit' button.
    //
    // The click event added to the 'Submit' button will trigger multiple
    // jQuery Ajax requests, one request per file.
    //
    // This implementation works, but there are some potential problems:
    //
    //   * According to the JSF Spec, each Ajax request must be handled
    //     using the request queue provided by jsf.js.
    //     This implementation skips the queue and triggers multiple
    //     ajax requests directly.
    //
    //   * We don't know if jsf.js includes any JSF-related request parameters
    //     that are missing in our jQuery Ajax requests.
    //
    // The second problem could be solved, because the file upload
    // plugin has a config option `formData` for adding additional form
    // data in the request.
    //
    init = function () {
        // Configure the file upload with our callbacks.
        $fileupload.find('input[type="file"]').fileupload({
            dataType: 'html',
            add: cb_add,
            send: cb_send,
            progress: cb_progress,
            progressall: cb_progressall,
            done: cb_done,
            fail: cb_failed
        });
        // Enable the submit button.
        $fileupload.find("input.submit-button").on("click", function (event) {
            event.preventDefault();
            $.each(dataSelections, function (i, data) {
                data.submit();
            });
        });
        // Enable the clear button
        $fileupload.find("input.clear-button").on("click", function (event) {
            location.reload();
        });
        // Remember the template for displaying progress message during upload in cb_progressall
        progressMsgTemplate = $fileupload.find('.upload-info-status span').html();
    };

    // cb_add()
    // -------------
    // The add callback is triggered when a file is added to the upload widget.
    cb_add = function (e, data) {
        // We assume that the singleFileUploads option is set,
        // i.e. each data selection contains exactly one file.
        // As a result, we will have one Ajax request per file
        // when we send the data.
        if (data.files.length !== 1) {
            throw "MyMAM's only works with the jQuery file upload plugin's singleFileUpload mode.";
        }
        var $tr = createTableRow(data);
        data.context = $tr;
        dataSelections.push(data);
        updateLineNumbers();
        makeSubmitButtonActive();
    };

    // cb_send()
    // ---------
    // This callback is triggered when the data is submitted.
    cb_send = function (e, data) {
        if (data.context) {
            // make status bar active
            data.context.find('.bar').addClass('active');
            // Hide the previous contents in the last column.
            data.context.find('.upload-info-status-column').children().css("display", "none");
            // Display "uploading" label in the last column.
            data.context.find('.upload-info-status-uploading').css("display", "");
        }
        if (!started) {
            startUpload();
            started = true;
        }
    };

    // cb_progress()
    // -------------
    // This callback is triggered when the progress bar can be updated for an individual file.
    cb_progress = function (e, data) {
        if (data.context) {
            var progress = parseInt(data.loaded / data.total * 100, 10);
            data.context.find('.bar').css('width', progress + '%');
        }
    };

    // cb_progressall()
    // ----------------
    // This callback is triggered when the global progress status can be updated.
    cb_progressall = function (e, data) {
        var progress = parseInt(data.loaded / data.total * 100, 10),
            $el = $fileupload.find('.upload-info-status');
        // The progress message may use the special variables $progress and $size.
        $el.html(progressMsgTemplate
            .replace('$progress', progress)
            .replace('$size', formatSize(data.total)));
    };

    // cb_done()
    // ---------
    // This callback is triggered when the upload for an individual file finished successfully.
    cb_done = function (e, data) {
        if (data.context) {
            // make status bar inactive
            data.context.find('.bar').removeClass('active');
            // Hide the previous contents in the last column.
            data.context.find('.upload-info-status-column').children().css("display", "none");
            // Display "done" label in the last column.
            data.context.find('.upload-info-status-done').css("display", "");
        }
        nDone = nDone + 1;
        if (nDone + nFailed >= dataSelections.length) {
            endUpload();
        }
    };

    // cb_failed()
    // -----------
    // This callback is called when the upload failed for an individual file.
    cb_failed = function (e, data) {
        if (data.context) {
            // make status bar inactive
            data.context.find('.bar').removeClass('active');
            // Hide the previous contents in the last column.
            data.context.find('.upload-info-status-column').children().css("display", "none");
            // Display "done" label in the last column.
            data.context.find('.upload-info-status-failed').css("display", "");
        }
        nFailed = nFailed + 1;
        if (nDone + nFailed >= dataSelections.length) {
            endUpload();
        }
    };

    // Helper function for creating a new row in the file table
    createTableRow = function (data) {
        var $tr = $fileupload.find('tr').first().clone();
        $tr.find('.upload-info-file-name').find('div').first().html(data.files[0].name);
        $tr.find('.upload-info-file-size').html(formatSize(data.files[0].size));
        $tr.css("display", "");
        $tr.appendTo('#fileupload table tbody');
        $tr.find('.upload-info-status-remove').click(function (event) {
            event.preventDefault();
            removeTableRow(data);
        });
        return $tr;
    };

    // Helper function for removing a data selection from the upload
    removeTableRow = function (data) {
        var index = dataSelections.indexOf(data);
        dataSelections.splice(index, 1);
        if (data.context) {
            data.context.remove();
        }
        if (dataSelections.length === 0) {
            makeSelectButtonActive();
        }
        updateLineNumbers();
    };

    // Re-calculate the line numbers in the left column of the file upload table.
    updateLineNumbers = function () {
        $fileupload.find('.upload-info-line-number').each(function (i, td) {
            $(td).html(i);
        });
    };

    // In the initial state, the primary button is "Add Files...".
    // When files have been added, the "Submit" button becomes the primary button.
    makeSubmitButtonActive = function () {
        $fileupload.find('.fileinput-button').removeClass('btn-primary');
        $fileupload.find('input.submit-button').addClass('btn-primary');
    };

    // When files are removed, "Add Files..." becomes the primary button again.
    makeSelectButtonActive = function () {
        $fileupload.find('input.submit-button').removeClass('btn-primary');
        $fileupload.find('.fileinput-button').addClass('btn-primary');
    };

    // Human readable file sizes.
    formatSize = function (bytes) {
        var
            kb = bytes / 1024.0,
            mb = kb / 1024.0,
            gb = mb / 2024.0;

        if (gb >= 1.0) {
            return twoDigit(gb) + "G";
        }
        if (mb >= 1.0) {
            return twoDigit(mb) + "M";
        }
        if (kb >= 1.0) {
            return twoDigit(kb) + "k";
        }
        return bytes + "b";
    };

    // Helper function for formatSize().
    twoDigit = function (d) {
        if (d >= 10) {
            return Math.round(d);
        }
        return Number(Math.round(d * 10.0) / 10.0).toFixed(1);
    };

    // When the upload is started, every button in the main button bar must be disabled.
    startUpload = function () {
        $fileupload.find('input.submit-button').removeClass('btn-primary');
        $fileupload.find('input.submit-button').addClass('disabled');
        $fileupload.find('input.submit-button').attr("disabled", true);
        $fileupload.find('.fileinput-button').removeClass('btn-primary');
        $fileupload.find('.fileinput-button').addClass('disabled');
        $fileupload.find('input[type="file"]').attr("disabled", true);
        $fileupload.find('.clear-button').addClass('btn-primary');
        $fileupload.find('.clear-button').addClass('disabled');
        $fileupload.find('.clear-button').attr('disabled', true);
    };

    // When the upload is done, a message is shown to the user and the Clear button is enabled.
    endUpload = function () {
        var $el;
        if (nFailed >= dataSelections.length) {
            $el = $fileupload.find('.upload-info-all-error');
        } else if (nFailed >= 1) {
            $el = $fileupload.find('.upload-info-all-warn');
        } else {
            $el = $fileupload.find('.upload-info-all-done');
        }
        // The messages may use the special variables $failed, $ok, and $total.
        $el.html($el.html()
            .replace("$failed", nFailed)
            .replace("$ok", nDone)
            .replace("$total", dataSelections.length));
        $el.css('display', '');
        $fileupload.find('.upload-info-status').css('display', 'none');
        $fileupload.find('.clear-button').removeClass('disabled');
        $fileupload.find('.clear-button').attr('disabled', false);
    };

    init();
});