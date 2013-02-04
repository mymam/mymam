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

if [ "$#" -ne 1 ] ; then
    echo "The number of generated files must be specified as command line parameter." >&2
    exit -1
fi

video_urls=(\
	"http://mirrors.creativecommons.org/movingimages/Building_On_The_Past.mov" \
        "http://ftp.nluug.nl/ftp/graphics/blender/apricot/trailer/Sintel_Trailer1.480p.DivX_Plus_HD.mkv" \
)

MEDIA_ROOT="/tmp/mymam-media-root"

if [ ! -d "${MEDIA_ROOT}" ] ; then
    mkdir -p "${MEDIA_ROOT}"
fi

if [ ! -d "${MEDIA_ROOT}" ] ; then
    echo "Failed to create ${MEDIA_ROOT}" >&2
    exit -1
fi

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SKEL="${SCRIPT_DIR}/skeleton"

for (( i=0; i<"$1"; i++ )) ; do
    video_url_index=`expr "$i" % ${#video_urls[@]}`
    DIR="${SKEL}/video"`expr ${video_url_index} + 1`
    if [ ! -d "${DIR}" ] ; then
        ${SCRIPT_DIR}/make-skeleton.sh "${video_urls[$video_url_index]}" "${DIR}"
        if [ $? -ne 0 ] ; then
            echo "make-skeleton.sh failed." >&2
            exit -1
        fi
    fi
    TEMPLATE=`date +%Y-%m-%d.XXXXXXXXXXXXXXXXXXX`
    TEMPLATE="${MEDIA_ROOT}/${TEMPLATE}"
    LINK=`mktemp -u "${TEMPLATE}"`
    ln -s "${DIR}" "${LINK}"
    ${SCRIPT_DIR}/post-to-server.sh `basename "${LINK}"`
    if [ $? -ne 0 ] ; then
        echo "post-to-server.sh failed." >&2
        exit -1
    fi
done
