# iText NDI


Based on iText 7.1.11

Uses the Hash Signing Service (HSS) from the NDI Sandbox (https://demo.sandbox.ndi.gov.sg/home) for signing PDF documents.
The docHashSignature is PAdES-LT compliant.
_**Modification of a docHashSignature appearance is not yet available**_

This repo contains two Maven artifacts:

1. *itext-ndi* - the library itself.
2. *ndi-demo*  - example of the library usage (play2 framework based web app).


#### Important note: 
As of now, (01.10.2020) the Sandbox is temporarily abandoned by NDI. 

The Sandbox API is behind compared to both the Staging and Production API environments, therefore it is highly unlikely this module would work out of the box on either Staging or Production, 
and moreover, **it is not guaranteed the NDI-demo would work correctly in the Sandbox** 
as a reliable API specification is not currently published on https://www.ndi-api.gov.sg/ (hopefully, this is only temporary).

You can find the HSS spec v 2.11 in the resources.  



## General information

The entire communication with the NDI API consists of six operations:

1. The signing request initialization call. 
2. Response containing signing reference
3. Callback with the user certificate.
4. The document hash signing call.
5. Empty response 
6. Callback with the signed hash.

Where steps 1-3 are called **the first leg** and 4-6 - **the second leg**


## How to use

 
This libary provides the DTO class (``NDIDocument``) together with the service (``NDIDocumentService``) for managing its states.

The full signing process consists of the following three calls: 

```
NDIDocument documentToSign = ndiDocumentService.init(aContent, aDocName, aNdiUserId, aFieldName, aNDIUserHint, aNdiSigningType);
//the first callback;
ndiDocumentService.updateFromCallback(NDIDocument aDocument, NdiCallbackMessage aMessage);
//the second callback;
ndiDocumentService.updateFromCallback(NDIDocument aDocument, NdiCallbackMessage aMessage);
byte[] signedPdf = documentToSign.getResult();
```

Where 
- ``aContent`` is the content of the source PDF
- ``aDocName`` - this is used as a document name during a signing session
- ``aNdiUserId`` - NDI identifier of the signer
- ``aNDIUserHint`` - ID of the user's access token that should be requested beforehand, using the NDI ASP service.
- ``aNdiSigningType`` - either `` Type.PN`` or ``Type.QR``.  This parameter defines the way the user accepts 
the signing session request. In the first case the push notification is sent sent to the user device. 
In the second one the user should scan a QR code.
This code is available after a successful response via ``NDIDocument#getQrCode()``


NDIDocumentService depends among others on the **IHssApiClient** which is used for sending requests to the API and handling responses.

However, it is up to the application to serve the URL for the NDI Callbacks (steps 3 and 6) _this url can be specified during the client registration using the template ``/<custom_prefix>/ndi/callback``_ .
Received query parameters must be converted into ``NDICallbackMessage`` by ``CallbackConverter``.
Then the mesage can be validated by ``CallbackValidator`` and then passed to ``NDIDocumentService``.

```
 NdiCallbackMessage callbackMessage = converter.convertParamsToCallbackMessage(aQueryParams);
 callbackValidator.validate(data);
 ndiDocumentService.updateFromCallback(documentToSign, callbackMessage);
```

#### CallbackValidator

Class for the callback validation. 
`` callbackValidator#validate(NdiCallbackMessage aCallback)`` checks whether this callback's nonce 
matches with one of the calls made by ``NDIDocumentService``

<em> There is a known bug that the API sends an incorrect nonce in the callback message for the second leg (6)</em>

 
## Interfaces 
``NdiDocumentService`` also depends on some interfaces. All of them have the default implementation.

#### IHssApiClient
Client interface of the NDI API hash signing service .

_Implemented by : ``NDIApiService``_

It is possible to customize the entire ``IHssApiClient`` 
or just ``IWebClient`` - which is being used for HTTPS requests underneath. 

#### IChallengeCodeGenerator
Generates the 4 digit challenge code that is being sent to the user device on the second leg to identify the signing operation. 

_Implemented by : ``ChallengeCodeGenerator``_
#### INonceGenerator
Generates a nonce which cannot be predicted, to ensure the challenge is always unique and not subjected to replay attacks.

_Implemented by : ``NonceGenerator``_
#### ITSAClient
Time Stamp Authority client interface from iText 7.

_Implemented by : ``FreeTSAClient``_
#### IChainGenerator
Creates a full certificate chain for the user certificate.

_Implemented by : ``ChainFromFileGenerator``_





