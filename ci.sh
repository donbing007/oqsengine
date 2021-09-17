uuid=$(dbus-uuidgen)
echo "generate uuid is ${uuid}"
mvn clean install -U -s ./.mvn/wrapper/settings.xml -Drequest.uuid=${uuid} -Dcontainer.server.port=9898
