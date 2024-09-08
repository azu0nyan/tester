
USER="root"
HOST="tester"

FILETOCOPY="../zioTester/target/universal/tester.zip"
FILE="tester.zip"
DIR="/srv/tester/"


ssh ${USER}@${HOST} sudo -S systemctl stop tester.service

ssh ${USER}@${HOST} rm /srv/${FILE}
ssh ${USER}@${HOST} rm -r ${DIR}"tester"

scp ${FILETOCOPY} ${USER}@${HOST}:/srv/${FILE}
ssh ${USER}@${HOST} unzip /srv/${FILE} -d ${DIR}

#scp appconfig.properties ${USER}@${HOST}:${DIR}online-test-suite-0.2-SNAPSHOT/bin/

#ssh ${USER}@${HOST} chown -R ${RUNUSER} ${DIR}

ssh ${USER}@${HOST} sudo -S systemctl start tester.service
