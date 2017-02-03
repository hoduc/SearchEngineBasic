#!/bin/sh

GRADLE_SRC=~/.gradle/wrapper/dists/gradle-2.10-all/a4w5fzrkeut1ox71xslb49gst/gradle-2.10/bin 
 
DEST=${PWD}
PROJ_NAME="dummy"

 
echo "Hello World. Path is ${DEST}"
 
while [[ $# > 1 ]]
do
    key="$1"
    case $key in
    -genproj)
        PROJ_NAME="$2"
	echo "CREATING PROJECT FOLDER ${PROJ_NAME} under ${PWD} (y/n)?"
	read ans
	if [[ "$ans" -eq y ]]
	then
	   echo "$(${GRADLE_SRC}/gradle init --type java-library)"
	fi
        shift
        ;;
    esac
    shift
done
