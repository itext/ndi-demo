
#NDI demo

Web application for signing pdf document by iText using HSS from NDI stack.
Based on Play2 framework  v 2.5.14 and iText 7.1.7 (will be updated soon )).
Application is using Google.Guice as DI engine.

##Before start:
1. Register client credentials for the NDI sandbox there:
https://demo.sandbox.ndi.gov.sg/clnreg/
2. Create a new configuration class which implements INDIConfig.
3. Bind INDIConfig to the created class in ApplicationModule
   bind(INDIConfig.class).to(NewNDIConfiguration.class);

## Start
The application can be started using:

- in development mode:
mvn play2:run -Dplay2.serverJvmArgs="-Dplay.server.pidfile.path=/dev/null -Dplay.server.dir=./target/classes"
- in prod mode
mvn play2:start

Default port is 9000.

It is possible to change using port by
-Dhttp.port=8080

##Additional
### Time stamp authority
By default, application uses free TSA (https://freetsa.org/tsr).
You free to use yourown TSAto change binding in ApplicationModule:
   bind(TSAClientBouncyCastle.class).to(FreeTSAClient.class);

###Secondary services












