#!/bin/bash

# MyMAM - Open Source Digital Media Asset Management.
# http://www.mymam.net
#
# Copyright 2013, MyMAM contributors as indicated by the @author tag.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

###########################################################################
# Helper script for data-generator.sh
# Uses the server REST interface to post a new video.
# @author fstab
###########################################################################

export BASE_URL="http://localhost:8080/mymam-server-0.1/rest"
export MEDIA_ROOT="/tmp/mymam-media-root"

function post_to_server () {

    ROOT_DIR="$1"

    if [ ! -f "${MEDIA_ROOT}/${ROOT_DIR}/generated/lowres.mp4" ] ; then
        echo "create_file() must be called with a ROOT_DIR as parameter." >&2
        exit -1
    fi

    #######################################################
    # POST new file
    #######################################################

    curl \
        --request "POST" \
        --header "Accept: application/json" \
        --header "Content-type: application/json" \
        --user "system:system" \
        --data "{ \"rootDir\": \"${ROOT_DIR}\", \"origFile\": \"video.mov\", \"uploadingUser\": \"admin\" }" \
        "${BASE_URL}/files"

    #######################################################
    # Read ID of new file
    #######################################################

    RESP=`curl \
        --silent \
        --request "GET" \
        --header "Accept: application/json" \
        --user "system:system" \
        "${BASE_URL}/files?status=NEW"`

    ID=`echo $RESP | sed -e 's/.*"id"://' | sed -e 's/,.*//'`

    echo "Creating file with ID $ID in dir ${ROOT_DIR}."

    #######################################################
    # Set status IN_PROGRESS
    #######################################################

    curl \
        --request "PUT" \
        --header "Accept: application/json" \
        --header "Content-type: application/json" \
        --user "system:system" \
        --data '"FILEPROCESSOR_IN_PROGRESS"' \
        "${BASE_URL}/files/${ID}/status"

    #######################################################
    # Put paths to generated files
    #######################################################

    curl \
        --request "PUT" \
        --header "Accept: application/json" \
        --header "Content-type: application/json" \
        --user "system:system" \
        --data '{ "lowResMp4": "generated/lowres.mp4", "lowResWebm": "generated/lowres.webm", "smallImg": "generated/small.jpg", "mediumImg": "generated/medium.jpg", "largeImg": "generated/large.jpg" }' \
        "${BASE_URL}/files/${ID}/generated-data"

    #######################################################
    # Set status DONE
    #######################################################

    curl \
        --request "PUT" \
        --header "Accept: application/json" \
        --header "Content-type: application/json" \
        --user "system:system" \
        --data '"FILEPROCESSOR_DONE"' \
        "${BASE_URL}/files/${ID}/status"
}

post_to_server "$1"
