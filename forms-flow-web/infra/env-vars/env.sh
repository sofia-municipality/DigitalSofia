#!/bin/sh
# line endings must be \n, not \r\n !
mkdir -p config
echo "window["\"_env_\""] = {" > ./config/config.js
ENV=${REACT_APP_ENV:=dev}
echo ${ENV}
awk -F '=' '{ print $1 ": " (ENVIRON[$1] ? "\"" ENVIRON[$1] "\"" : $2) "," }' ./.env.${ENV} >> ./config/config.js
echo "}" >> ./config/config.js