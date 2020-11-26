##
Web application 
Paly2 framework 

## Start
The application can be started using:

- in development mode:
mvn play2:run -Dplay2.serverJvmArgs="-Dplay.server.pidfile.path=/dev/null -Dplay.server.dir=./target/classes -Dndi.client.id=clientid -Dndi.client.secret=clientsecret"

- in prod mode
mvn play2:start -Dplay2.serverJvmArgs="-Dndi.client.id=clientid -Dndi.client.secret=clientsecret"


Default port is 9000.

It is possible to change using port by
-Dhttp.port=8080



