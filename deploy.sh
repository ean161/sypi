#!/bin/bash

source .env

REMOTE_USER="root"
REMOTE_HOST="vps.4n.id.vn"
REMOTE_PATH="/desktop/eanmc/plugins"
JAR_NAME="sypi-1.0.jar"
PASSWORD="$SSH_PASSWORD"
SERVER_DIR="/desktop/eanmc"
SERVER_JAR="server.jar"
TMP_DIR="$SERVER_DIR/tmp"

echo -e "\033[1m[SERVER] \033[31mSTOPPING MINECRAFT SERVER\033[0m"
sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "pkill -f $SERVER_JAR || true"

echo -e "\033[1m[BUILD] \033[34mBUILDING A PLUGIN\033[0m"
mvn clean package || { echo -e "\033[1;31m[ERROR] Build thất bại\033[0m"; exit 1; }

echo -e "\033[1m[REMOTE] \033[31mREMOVING OLD PLUGIN VERSION\033[0m"
sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "rm -f $REMOTE_PATH/$JAR_NAME"

echo -e "\033[1m[UPLOAD] \033[34mUPLOADING NEW PLUGIN\033[0m"
sshpass -p "$PASSWORD" scp -o StrictHostKeyChecking=no target/$JAR_NAME $REMOTE_USER@$REMOTE_HOST:$REMOTE_PATH

echo -e "\033[1m[UPLOAD] \033[32mPLUGIN WAS UPLOADED TO SERVER\033[0m"

echo -e "\033[1m[SERVER] \033[34mSTARTING MINECRAFT SERVER\033[0m"
sshpass -p "$PASSWORD" ssh -o StrictHostKeyChecking=no $REMOTE_USER@$REMOTE_HOST "
    sleep 2;
    cd $SERVER_DIR && java -Djava.io.tmpdir=$TMP_DIR -jar $SERVER_JAR > log.out 2>&1 &
    exit
"
echo -e "\033[1m[SERVER] \033[32mSERVER RESTARTED\033[0m"
