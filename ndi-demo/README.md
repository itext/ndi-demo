# NDI-DEMO
A web application for signing PDF documents with iText using HSS from the NDI stack.
Based on Play2 framework v 2.5.14 and itext-ndi (com.itextpdf%itext-ndi).
The application is using Google.Guice as a DI engine.


## Before you start:

The NDI API is designed to be used from a web-application. 
To gain access to the HSS an application must be registered as an NDI **digital signing partner**, using the [registration form](https://demo.sandbox.ndi.gov.sg/clnreg/).

After completing the registration you will receive `ClientId` and `ClientSecret` which should be used for the API calls.

To configure the App using these credentials you need to implement the INDIInstanceConfig interface and then inject that class in ApplicationModule

   `bind(INDIInstanceConfig.class).to(NewNDIConfiguration.class);`

**Note:**

This demo is configured to serve `/api/ndi/callback` as the callback URL. Therefore you should either modify `conf/routes` file or set up `api` as a callback URL prefix in the last step of the registration.


## Start
The application can be started using:

- in Development mode:

`mvn play2:run -Dplay2.serverJvmArgs="-Dplay.server.pidfile.path=/dev/null -Dplay.server.dir=./target/classes"`

- in Production mode:

`mvn play2:start`



It is possible to change the port used *(9000 by default)* e.g.:

`-Dhttp.port=8080`

## Additional
### Time Stamp Authority
By default, the application uses a free TSA (https://freetsa.org/tsr).

You can use your own TSA client by changing the binding in ApplicationModule:

`bind(TSAClientBouncyCastle.class).to(FreeTSAClient.class);`













