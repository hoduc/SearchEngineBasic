#!/bin/sh
#!/bin/bash

#read core variable
RUN_CONF="run_conf.properties"
CORE="dummy"
while IFS= read -r line || [ -n "$line" ];do
    echo "${line}"
    k="${line%=*}" #everything before=
    v="${line#*=}" #everything after=
    echo "k=${k},v=${v}"
    if [[ "${k}" -eq "CORE" ]]
    then
	CORE="${v}"
    fi
done < "${RUN_CONF}"

echo "CORE=${CORE}"


#flask
FLASK_APP_SRC="front_end/test_flask.py"
REMOVE_OLD_FLASK_APP="rm front_end/test_flask.pyc"
EXPORT_FLASK="export FLASK_APP=${FLASK_APP_SRC}"
RUN_FLASK="python -m flask run"

#python output
PATCH_SRC="patch/"
PATCH_JSON="${PATCH_SRC}patch_link.json"
PATCH_PR="${PATCH_SRC}pagerank.txt"
RUN_PR="python page_rank.py"

#solr
SCHEMA="managed-schema"
SOLR_CONF="solrconfig.xml"
CURL="curl"
SOLR="solr"
SOLR_HOME="http://localhost:8983/solr"
SOLR_FOLDER="solr-6.2.1"
SOLR_CORE_FOLDER="${SOLR_FOLDER}/server/solr/${CORE}"
SOLR_CORE_CONFIG_FOLDER="${SOLR_CORE_FOLDER}/conf"
SOLR_BINARY="bin/solr"
SOLR_POST_BINARY="bin/post"
START_COMMAND="${SOLR_BINARY} start"
STOP_COMMAND="${SOLR_BINARY} stop"
POST_COMMAND="${SOLR_POST_BINARY} -c "
CREATE_CORE_COMMAND="${SOLR_BINARY} create_core -c ${CORE}"
UPDATE_COMMAND="${CURL} ${SOLR_HOME}/${CORE}/update?commit=true --data-binary @${PATCH_JSON} -H Content-type:application/json"
RELOAD_COMMAND="${CURL} \"${SOLR_HOME}/admin/cores?action=RELOAD&core=${CORE}\""
EXT_PR_FILE_NAME="external_pageRankFile.txt"
COPY_EXT_PR_FILE="cp ${PATCH_PR} ${SOLR_CORE_FOLDER}/data/${EXT_PR_FILE_NAME}"
COPY_SCHEMA_CONFIG="cp config/{${SCHEMA},${SOLR_CONF}} ${SOLR_CORE_CONFIG_FOLDER}/"

CRAWLED_DATA="CrawledData"
SOLR_START="${SOLR_FOLDER}/${START_COMMAND}"
SOLR_STOP="${SOLR_FOLDER}/${STOP_COMMAND}"
SOLR_POST="${SOLR_FOLDER}/${POST_COMMAND}${CORE} ${CRAWLED_DATA}/"
SOLR_UPDATE="${UPDATE_COMMAND}"
SOLR_CC="${SOLR_FOLDER}/${CREATE_CORE_COMMAND}"
SOLR_RELOAD="${RELOAD_COMMAND}"

RUN_PR_DESCRIPTION="Compute pagerank, json patch file...\n"
COPY_EXT_PR_FILE_DESCRIPTION="Copy pagerank_file.txt to ${CORE} data folder...\n"
SOLR_START_DESCRIPTION="Starting solr...\n"
SOLR_POST_DESCRIPTION="Start indexing...\n"
SOLR_UPDATE_DESCRIPTION="Patching field link....\n"
SOLR_CREATE_CORE_DESCRIPTION="Creating core ${CORE} ...\n"
COPY_SCHEMA_CONFIG_DESCRIPTION="cp config/{${SCHEMA},${SOLR_CONF}} into ${SOLR_CORE_CONFIG_FOLDER}/"
SOLR_RELOAD_DESCRIPTION="Reload solr ${CORE} to apply new config/schema...\n"

HELP="?\n"
REMOVE_OLD_FLASK_APP_DESCRIPTION="Removing old flask app...\n"
EXPORT_FLASK_DESCRIPTION="Exporting flask app...\n"
RUN_FLASK_DESCRIPTION="Preparing to run Flask server...\n"

MANDATE_ORDER=("${SOLR_START}" "${SOLR_CC}" "${COPY_SCHEMA_CONFIG}" "${SOLR_RELOAD}" "${RUN_PR}" "${COPY_EXT_PR_FILE}" "${SOLR_POST}" "${SOLR_UPDATE}" "${REMOVE_OLD_FLASK_APP}" "${EXPORT_FLASK}" "${RUN_FLASK}")
MANDATE_DESCRIPTION=( "${SOLR_START_DESCRIPTION}" "${SOLR_CREATE_CORE_DESCRIPTION}" "${COPY_SCHEMA_CONFIG_DESCRIPTION}" "${SOLR_RELOAD_DESCRIPTION}" "${RUN_PR_DESCRIPTION}" "${COPY_EXT_PR_FILE_DESCRIPTION}" "${SOLR_POST_DESCRIPTION}" "${SOLR_UPDATE_DESCRIPTION}" "${REMOVE_OLD_FLASK_DESCRIPTION}" "${EXPORT_FLASK_DESCRIPTION}" "${RUN_FLASK_DESCRIPTION}")
EXE_ORDER=()
EXE_DESCRIPTION=()
mandate=0

for arg in "$@"
do
    case $arg in
    -start)
	mandate=1
	break
	;;
    -startsolr)
	EXE_DESCRIPTION+=("${SOLR_START_DESCRIPTION}")
	EXE_ORDER+=("${SOLR_START}")
	;;
    -cc)
	EXE_DESCRIPTION+=("${SOLR_CREATE_CORE_DESCRIPTION}")
	EXE_ORDER+=("${SOLR_CC}")
	;;
    -copy-schema-conf)
	EXE_DESCRIPTION+=("${COPY_SCHEMA_CONFIG_DESCRIPTION}")
	EXE_ORDER+=("${COPY_SCHEMA_CONFIG}")
	;;
    -reload-solr)
	EXE_DESCRIPTION+=("${SOLR_RELOAD_DESCRIPTION}")
	EXE_ORDER+=("${SOLR_RELOAD}")
	;;
    -runpr)
	EXE_DESCRIPTION+=("${RUN_PR_DESCRIPTION}")
	EXE_ORDER+=("${RUN_PR}")
	;;
    -copypr)
	EXE_DESCRIPTION+=("${COPY_EXT_PR_FILE_DESCRIPTION}")
	EXE_ORDER+=("${COPY_EXT_PR_FILE}")
	;;
    -solr-start-index)
	EXE_DESCRIPTION+=("${SOLR_POST_DESCRIPTION}")
	EXE_ORDER+=("${SOLR_POST}")
	;;
    -solr-patch-json)
	EXE_DESCRIPTION+=("${SOLR_UPDATE_DESCRIPTION}")
	EXE_ORDER+=("${SOLR_UPDATE}")
	;;
    -flask-run)
	EXE_DESCRIPTION+=("${REMOVE_OLD_FLASK_APP_DESCRIPTION}")
	EXE_DESCRIPTION+=("${EXPORT_FLASK_DESCRIPTION}")
	EXE_DESCRIPTION+=("${RUN_FLASK_DESCRIPTION}")
	EXE_ORDER+=("${REMOVE_OLD_FLASK_APP}")
	EXE_ORDER+=("${EXPORT_FLASK}")
	EXE_ORDER+=("${RUN_FLASK}")
    esac
done


if [[ $# -eq 0 ]]
then
    echo "${HELP}"
else
    echo "mandate:${mandate}"
    if [[ "${mandate}" -eq 1 ]]
    then
	echo "Start the whole process"
	for i in "${!MANDATE_ORDER[@]}" #not space separated
	do
	    echo "${MANDATE_DESCRIPTION[$i]}"
	    echo "${MANDATE_ORDER[$i]}"
	done
    else
	for i in "${!EXE_ORDER[@]}" #not space separated
	do
	    echo "${EXE_DESCRIPTION[$i]}"
	    eval "${EXE_ORDER[$i]}"
	done
    fi
fi
