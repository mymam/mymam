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
# @author fstab
#
# This script is called by data-generator.sh, you don't need do
# call it manually.
#
# The script creates a skeleton directory with the following contents:
#
# skeleton/Building_On_The_Past.mov
# skeleton/generated/lowres.mp4
# skeleton/generated/lowres.webm
# skeleton/generated/small.jpg
# skeleton/generated/medium.jgp
# skeleton/generated/large.jpg
###########################################################################

if [ "$#" -ne 1 ] ; then
    echo "Usage: make-skeleton.sh <path>" >&2
    exit -1
fi

export SKEL="$1"

if [ -e ${SKEL} ] ; then
    echo "${SKEL} already exists." >&2
    exit -1
fi

command -v avconv

if [ $? -ne 0 ] ; then
    echo "avconv is not installed." >&2
    exit -1
fi

mkdir ${SKEL}

if [ $? -ne 0 ] ; then
    echo "mkdir ${SKEL} failed." >&2
    exit -1
fi

cd ${SKEL}
wget http://mirrors.creativecommons.org/movingimages/Building_On_The_Past.mov

if [ $? -ne 0 ] ; then
    echo "Failed to download example video." >&2
    exit -1
fi

export GENERATED=generated
mkdir ${GENERATED}

avconv -i Building_On_The_Past.mov -c:v libx264 -b 4000k ${GENERATED}/lowres.mp4
if [ $? -ne 0 ] ; then
    echo "Failed to convert the video to MP4. May be you need to install libx264 support for avconv?" >&2
    exit -1
fi

avconv -i Building_On_The_Past.mov ${GENERATED}/lowres.webm
if [ $? -ne 0 ] ; then
    echo "Failed to convert the video to WEBM." >&2
    exit -1
fi

generate_image () {

    INPUT_FILE=$1
    OUTPUT_FILE=$2
    MAX_WIDTH=$3
    MAX_HEIGHT=$4

    avconv -i $INPUT_FILE -vsync 1 -r 1 -an -y -vf \
        "scale=iw*sar*min($MAX_WIDTH/(iw*sar)\,$MAX_HEIGHT/ih):ih*min($MAX_WIDTH/(iw*sar)\,$MAX_HEIGHT/ih)" \
        -vframes 1 $OUTPUT_FILE
    
    if [ $? -ne 0 ] ; then
        echo "Failed to convert video frame to $OUTPUT_FILE" >&2
        exit -1
    fi
}

generate_image Building_On_The_Past.mov ${GENERATED}/small.jpg 100 75
generate_image Building_On_The_Past.mov ${GENERATED}/medium.jpg 200 150
generate_image Building_On_The_Past.mov ${GENERATED}/large.jpg 400 300
