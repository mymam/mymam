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
# Script to import test data into MyMAM.
# @author fstab
###########################################################################

export BASE_URL="http://localhost:8080/mymam-server-0.1/rest"
export MEDIA_ROOT="/tmp/mymam-media-root"

function create_file () {

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
        --data '{ "lowResMp4": "generated/lowres.mp4", "lowResWebm": "generated/lowres.webm", "previewImg": "generated/large.jpg", "thumbnail": "generated/small.jpg" }' \
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


SRC="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SKEL="${SRC}/skeleton"

if [ "$#" -ne 1 ] ; then
    echo "The number of generated files must be specified as command line parameter." >&2
    exit -1
fi

if [ ! -e "${SKEL}" ] ; then
    ${SRC}/make-skeleton.sh "${SKEL}"
    if [ $? -ne 0 ] ; then
        echo "make-skeleton.sh failed." >&2
        exit -1
    fi
fi

if [ ! -d "${MEDIA_ROOT}" ] ; then
    mkdir "${MEDIA_ROOT}"
fi

TEMPLATE=`date +%Y-%m-%d.XXXXXXXXXXXXXXXXXXX`
TEMPLATE="${MEDIA_ROOT}/${TEMPLATE}"

for i in `seq 1 "$1"` ; do
    LINK=`mktemp -u "${TEMPLATE}"`
    ln -s "${SKEL}" "${LINK}"
    create_file `basename "${LINK}"`
done
