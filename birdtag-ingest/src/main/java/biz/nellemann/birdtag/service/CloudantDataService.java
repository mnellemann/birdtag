package biz.nellemann.birdtag.service;

import com.ibm.cloud.cloudant.v1.model.*;
import com.ibm.cloud.sdk.core.service.exception.NotFoundException;
import com.ibm.cloud.sdk.core.service.exception.ServiceResponseException;
import io.micronaut.context.annotation.Property;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.cloud.cloudant.v1.Cloudant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class CloudantDataService {

    private static final Logger log = LoggerFactory.getLogger(CloudantDataService.class);


    @Property(name = "cloudant.service.url")
    protected String SERVICE_URL;

    @Property(name = "cloudant.api.key")
    protected String API_KEY;


    private Cloudant cloudantService;


    @PostConstruct
    public void init() {
        // Create the authenticator.
        IamAuthenticator authenticator = new IamAuthenticator.Builder()
            .apikey(API_KEY)
            .build();

        cloudantService = new Cloudant("cloudant", authenticator);
        cloudantService.setServiceUrl(SERVICE_URL);
    }

    public void test() {
        ServerInformation response = cloudantService.getServerInformation().execute().getResult();
        System.out.println(response);

        createDatabase("test");
        createDocument("test", "foobar");
        updateDocument("test", "foobar");
    }


    // 2. Create a database ===============================================
    public void createDatabase(String databaseName) {
        PutDatabaseOptions putDbOptions =
            new PutDatabaseOptions.Builder().db(databaseName).build();

        // Try to create database if it doesn't exist
        try {
            Ok putDatabaseResult = cloudantService
                .putDatabase(putDbOptions)
                .execute()
                .getResult();

            if (putDatabaseResult.isOk()) {
                log.info("Database created: {}", databaseName);
            }
        } catch (ServiceResponseException sre) {
            log.warn("createDatabase() - Service Response Status Code: {}", sre.getStatusCode());
            if (sre.getStatusCode() == 412)
                log.error("createDatabase() - Database already exists: {}", databaseName);
        }

    }



    // 3. Create a document ===============================================
    public void createDocument(String databaseName, String documentName) {

        Document newDocument = new Document();

        // Setting id for the document is optional when "postDocument" method is used for CREATE.
        // When id is not provided the server will generate one for your document.
        newDocument.setId(documentName);

        // Add "name" and "joined" fields to the document
        newDocument.put("name", "Bob Smith");
        newDocument.put("joined", "2019-01-24T10:42:59.000Z");

                // Save the document in the database with "postDocument" method
        PostDocumentOptions createDocumentOptions =
            new PostDocumentOptions.Builder()
                .db(databaseName)
                .document(newDocument)
                .build();

        try {
            DocumentResult createDocumentResponse = cloudantService
                .postDocument(createDocumentOptions)
                .execute()
                .getResult();

            newDocument.setRev(createDocumentResponse.getRev());
            log.info("createDocument() - Document created: {}", documentName);

        } catch (ServiceResponseException sre) {
            log.warn("createDocument() - Service Response Status Code: {}", sre.getStatusCode());
            log.error(sre.getMessage());
        }
    }


    public void updateDocument(String databaseName, String documentName) {

        GetDocumentOptions documentInfoOptions =
            new GetDocumentOptions.Builder()
                .db(databaseName)
                .docId(documentName)
                .build();

        try {

            // Try to get the document if it previously existed in the database
            Document document = cloudantService
                .getDocument(documentInfoOptions)
                .execute()
                .getResult();

            // Note: for response byte stream use:
            /*
            InputStream documentAsByteStream =
                client.getDocumentAsStream(documentInfoOptions)
                    .execute()
                    .getResult();
            */

            // Add Bob Smith's address to the document
            document.put("address", "19 Front Street, Darlington, DL5 1TY");

            // Remove the joined property from document object
            document.removeProperty("joined");

            // Update the document in the database
            PostDocumentOptions updateDocumentOptions =
                new PostDocumentOptions.Builder()
                    .db(databaseName)
                    .document(document)
                    .build();

            // ================================================================
            // Note: for request byte stream use:
            /*
            PostDocumentOptions updateDocumentOptions =
                new PostDocumentOptions.Builder()
                    .db(exampleDbName)
                    .contentType("application/json")
                    .body(documentAsByteStream)
                    .build();
            */
            // ================================================================

            DocumentResult updateDocumentResponse = cloudantService
                .postDocument(updateDocumentOptions)
                .execute()
                .getResult();

            // Keeping track of the latest revision number of the document object
            // is necessary for further UPDATE/DELETE operations:
            document.setRev(updateDocumentResponse.getRev());
            log.info("updateDocument() - Document updated: {}", documentName);

        } catch (NotFoundException nfe) {
            log.error("updateDocument() - Document update failed: ({}) {} ", nfe.getStatusCode(), documentName);
        }

    }


}
