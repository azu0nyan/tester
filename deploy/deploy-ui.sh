USER="root"
HOST="tester"

FILETOCOPY1="../testerUI/data/index.html"
FILETOCOPY2="../testerUI/data/index.css"
FILETOCOPY3="../testerUI/target/scala-3.2.2/scalajs-bundler/main/testerui-fastopt-bundle.js"
FILE1="index.html"
FILE2="index.css"
FILE3="main.js"
DIR="/srv/tester-ui/"

scp ${FILETOCOPY1} ${USER}@${HOST}:${DIR}${FILE1}
scp ${FILETOCOPY2} ${USER}@${HOST}:${DIR}${FILE2}
scp ${FILETOCOPY3} ${USER}@${HOST}:${DIR}${FILE3}
