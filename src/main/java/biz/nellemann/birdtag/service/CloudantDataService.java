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

import java.util.Collections;
import java.util.Map;

@Singleton
public class CloudantDataService {

    // https://github.com/ibm/cloudant-java-sdk

    private static final Logger log = LoggerFactory.getLogger(CloudantDataService.class);


    @Property(name = "cloudant.service.url")
    protected String SERVICE_URL;

    @Property(name = "cloudant.api.key")
    protected String API_KEY;

    @Property(name = "cloudant.database")
    protected String DATABASE;

    private Cloudant cloudantService;


    @PostConstruct
    public void init() {
        // Create the authenticator.
        IamAuthenticator authenticator = new IamAuthenticator.Builder()
            .apikey(API_KEY)
            .build();

        cloudantService = new Cloudant("cloudant", authenticator);
        cloudantService.setServiceUrl(SERVICE_URL);

        try {
            createDatabase();
            createDesignDocuments();
        } catch (ServiceResponseException ignored) {

        }
    }


    public void test() {
        ServerInformation response = cloudantService.getServerInformation().execute().getResult();
        System.out.println(response);
    }


    private void createDatabase() {
        PutDatabaseOptions putDbOptions =
            new PutDatabaseOptions.Builder().db(DATABASE).build();

        // Try to create database if it doesn't exist
        try {
            Ok putDatabaseResult = cloudantService
                .putDatabase(putDbOptions)
                .execute()
                .getResult();

            if (putDatabaseResult.isOk()) {
                log.info("createDatabase() - Database created: {}", DATABASE);
            }

        } catch (ServiceResponseException sre) {
            if (sre.getStatusCode() == 412) {
                log.info("createDatabase() - Database already exists: {}", DATABASE);
            } else {
                log.warn("createDatabase() - Service Response Status Code: {}", sre.getStatusCode());
            }
        }

    }


    private void createDesignDocuments() {

        DesignDocumentViewsMapReduce newImages =
            new DesignDocumentViewsMapReduce.Builder()
                .map("function(doc) { if(doc.status === \"new\") { emit(doc.url, [doc.url, doc.timestamp]); }}")
                .build();

        DesignDocumentViewsMapReduce taggedImages =
            new DesignDocumentViewsMapReduce.Builder()
                .map("function(doc) { if(doc.status === \"tagged\") { emit(doc.url, [doc.url, doc.timestamp]); }}")
                .build();

        DesignDocumentViewsMapReduce untaggedImages =
            new DesignDocumentViewsMapReduce.Builder()
                .map("function(doc) { if(doc.status === \"untagged\") { emit(doc.url, [doc.url, doc.timestamp]); }}")
                .build();


        SearchIndexDefinition activeIndex =
            new SearchIndexDefinition.Builder()
                .index("function(doc) { index(\"timestamp\", doc.timestamp); index(\"active\", doc.active); index(\"status\", doc.status); }")
                .build();


        DesignDocument designDocument = new DesignDocument();
        designDocument.setViews(Collections.singletonMap("getNewImages", newImages));
        designDocument.setViews(Collections.singletonMap("getTaggedImages", taggedImages));
        designDocument.setViews(Collections.singletonMap("getUntaggedImages", untaggedImages));
        designDocument.setIndexes(Collections.singletonMap("activeImages", activeIndex));


        PutDesignDocumentOptions designDocumentOptions =
            new PutDesignDocumentOptions.Builder()
                .db(DATABASE)
                .designDocument(designDocument)
                .ddoc("allimages")
                .build();

        DocumentResult allusersResponse =
            cloudantService.putDesignDocument(designDocumentOptions).execute()
                .getResult();

        System.out.println(allusersResponse);
    }



    public String createDocument(Map<String,Object> properties) {

        Document newDocument = new Document();
        newDocument.setProperties(properties);
        PostDocumentOptions createDocumentOptions =
            new PostDocumentOptions.Builder()
                .db(DATABASE)
                .document(newDocument)
                .build();
        try {
            DocumentResult createDocumentResponse = cloudantService
                .postDocument(createDocumentOptions)
                .execute()
                .getResult();

            newDocument.setRev(createDocumentResponse.getRev());
            log.info("createDocument() - Document created: {}", createDocumentResponse.getId());
            return createDocumentResponse.getId();

        } catch (ServiceResponseException sre) {
            log.warn("createDocument() - Service Response Status Code: {}", sre.getStatusCode());
            log.error(sre.getMessage());
        }

        return null;
    }


    public void updateDocument(String documentName) {

        GetDocumentOptions documentInfoOptions =
            new GetDocumentOptions.Builder()
                .db(DATABASE)
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
                    .db(DATABASE)
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

    public Map<String,Object> getDocument(String documentId) {
        GetDocumentOptions documentInfoOptions =
            new GetDocumentOptions.Builder()
                .db(DATABASE)
                .docId(documentId)
                .build();

        try {

            // Try to get the document if it previously existed in the database
            Document document = cloudantService
                .getDocument(documentInfoOptions)
                .execute()
                .getResult();

            document.get("url");
            return document.getProperties();

        } catch (ServiceResponseException sre) {
            log.error("getDocument() - Service Response Status Code: {}", sre.getStatusCode());
        }

        return null;
    }

    public Map<String, Object> latestDocument() {

        PostSearchOptions searchOptions = new PostSearchOptions.Builder()
            .db(DATABASE)
            .ddoc("allimages")
            .index("activeImages")
            .query("status:new")
            .limit(1)
            .build();

        SearchResult response =
            cloudantService.postSearch(searchOptions).execute()
                .getResult();

        if(response.getTotalRows() == 1) {
            return getDocument(response.getRows().get(0).getId());
        }

        System.out.println(response);
        return null;
    }

}
