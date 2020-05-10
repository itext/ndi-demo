#IText NDI
Based on iText 7.1.11

Uses the Hash Signing Service (HSS) from NDI stack (https://www.ndi-api.gov.sg/) for signing Pdf documents.
The signature is a Pades-LT compliant.

_**Modification of a signature appearance is not available yet**_

##Before start:

NDI API is designed to be used from web-application. 
For getting an access to the HSS an application must be registered as a NDI`s **digital signing partner**, using the [registration form](https://demo.sandbox.ndi.gov.sg/clnreg/)
Completing the registration you will get ``ClientId`` and  ``ClientSecret`` which should be used for the API calls.


## General information

The entire communication to NDI APi consist of 6 operations:

1. The signing request initialization call. 
2. Response containing signing reference
3. Callback with the user certificate.
4. The document hash signing call.
5. Empty response 
6. Callback with the signed hash.

Where steps 1-3 are called **the first leg** and 4-6 - **the second leg**


##How to use

This libary provides the DTO class (``NDIDocument``) together with the service (``NDIDocumentService``) for managing its states.

The full signing process consist of 3 consequent calls. 

``
NDIDocument documentToSign = ndiDocumentService.init(aContent, aDocName, aNdiUserId, aFieldName, aNDIUserHint, aNdiSigningType);
//the first callback;
ndiDocumentService.updateFromCallback(NDIDocument aDocument, NdiCallbackMessage aMessage);
//the second callback;
ndiDocumentService.updateFromCallback(NDIDocument aDocument, NdiCallbackMessage aMessage);
byte[] signedPdf = documentToSign.getResult();
``

Where 
- ``aContent`` is the content of the source pdf
- ``aDocName`` - that is used as a document name during a signing session
- ``aNdiUserId`` - ndi identifier of the signer
- ``aNDIUserHint`` - id of the user`s access token that should be requested beforehand, using the NDI ASP service.
- ``aNdiSigningType`` - either `` Type.PN`` or ``Type.QR``.  This parameter defines the way the user accept 
the signing session request in the first case the push notification is being sent to user device 
in the second one the user should scan qr code.
That code is available after success response via ``NDIDocument#getQrCode()``


NDIDocumentService depends among others on the **IHssApiClient** which is used for sending requests to API and handling responses.

However it is on the application to serve the URL for the NDI Callbacks (steps 3 and 6) _this url can be specified during the client registration using the template ``/<custom_prefix>/ndi/callback``_ .
Received query parameters must be converted into ``NDICallbackMessage`` by ``CallbackConverter``.
Then the mesage can be validated by ``CallbackValidator`` next passed to ``NDIDocumentService``.

``
 NdiCallbackMessage callbackMessage = converter.convertParamsToCallbackMessage(aQueryParams);
 callbackValidator.validate(data);
 ndiDocumentService.updateFromCallback(documentToSign, callbackMessage);
``

####CallbackValidator

Class for the callback validation. 
`` callbackValidator#validate(NdiCallbackMessage aCallback)`` checks whether this callback`s nonce 
matches to one of the calls made by  ``NDIDocumentService``

<em> There is a known bug that API sends incorrect nonce in the callback message for the second leg (6)</em>

 
##Interfaces 
``NdiDocumentService`` also depends on some interfaces. All of them have default implementation.

####IHssApiClient
Client interface of NDI API hash signing service .

_Implemented by : ``NDIApiService``_

It is possible to customize the entire ``IHssApiClient`` 
or just ``IWebClient`` - which is being used for Https requests underneath. 

####IChallengeCodeGenerator
Generates the 4 digits challenge code that is being sent to the user device on the second leg to identify the signing operation. 

_Implemented by : ``ChallengeCodeGenerator``_
####INonceGenerator
Generates  a nonce which cannot be predicted, to ensure the challenge is always unique and not subjected to replay attacks.

_Implemented by : ``NonceGenerator``_
####ITSAClient
Time Stamp Authority client interface from IText 7.

_Implemented by : ``FreeTSAClient``_
####IChainGenerator
Creates full certificate chain for the user certificate.

_Implemented by : ``ChainFromFileGenerator``_



### NDI-DEMO
There is an example of the library usage in the real web app:
com.itextpdf%ndi-demo

That`s a fully functional pay2 framework based web app which is using itext-ndi 
library.

