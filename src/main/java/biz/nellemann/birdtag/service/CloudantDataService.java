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

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

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
            createDesignDocuments2();
        } catch (ServiceResponseException ignored) {

        }
    }


    public String test() {
        ServerInformation response = cloudantService.getServerInformation().execute().getResult();
        return response.toString();
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


    private void createDesignDocuments2() {

        SearchIndexDefinition activeIndex =
            new SearchIndexDefinition.Builder()
                .index("function(doc) { index(\"timestamp\", doc.timestamp); index(\"active\", doc.active); index(\"status\", doc.status); }")
                .build();

        DesignDocument designDocument = new DesignDocument();
        designDocument.setIndexes(Collections.singletonMap("activeTags", activeIndex));


        PutDesignDocumentOptions designDocumentOptions =
            new PutDesignDocumentOptions.Builder()
                .db(DATABASE)
                .designDocument(designDocument)
                .ddoc("alltags")
                .build();

        DocumentResult allImagesResult =
            cloudantService.putDesignDocument(designDocumentOptions).execute()
                .getResult();

        System.out.println(allImagesResult);
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

        DocumentResult allImagesResult =
            cloudantService.putDesignDocument(designDocumentOptions).execute()
                .getResult();

        System.out.println(allImagesResult);
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


    public void updateDocument(String documentName, String key, Object value) {

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

            // Add Bob Smith's address to the document
            //document.put("address", "19 Front Street, Darlington, DL5 1TY");
            document.put(key, value);
            document.put("status", "tagged");

            // Remove the joined property from document object
            //document.removeProperty("joined");

            // Update the document in the database
            PostDocumentOptions updateDocumentOptions =
                new PostDocumentOptions.Builder()
                    .db(DATABASE)
                    .document(document)
                    .build();

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

    public List<?> find(String prefix) {

        ArrayList<Map<String,Object>> list = new ArrayList<>();

        Map<String, Object> selector = Collections.singletonMap(
            "_id",
            Collections.singletonMap("$beginsWith", prefix));

        Map<String, String> fieldSort = Collections.singletonMap("timestamp", "desc");

        PostFindOptions findOptions = new PostFindOptions.Builder()
            .db(DATABASE)
            .selector(selector)
            .fields(Arrays.asList("_id", "timestamp", "name"))
            //.addSort(fieldSort)
            .limit(25)
            .build();

        FindResult response =
            cloudantService.postFind(findOptions).execute()
                .getResult();

        response.getDocs().forEach(doc -> {
            log.info("doc: {}", doc.toString());
            list.add(getDocument(doc.getId()));
        });

        System.out.println(response);
        log.info("list(): {}", list);

        return list;
    }

    public Map<String,Object> getDocument(String documentId) {

        Map<String,Object> properties = null;

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
            properties = document.getProperties();
            properties.put("id", documentId);

        } catch (ServiceResponseException sre) {
            log.error("getDocument() - Service Response Status Code: {}", sre.getStatusCode());
        }

        log.info("getDocument() - {}", properties);
        return properties;
    }


    public Map<String, Object> latestDocument() {

        Map<String, Object> responseMap = new HashMap<>();

        PostSearchOptions searchOptions = new PostSearchOptions.Builder()
            .db(DATABASE)
            .ddoc("allimages")
            .index("activeImages")
            .query("status:new")
            .build();

        SearchResult response =
            cloudantService.postSearch(searchOptions).execute()
                .getResult();

        if(response.getTotalRows() > 0) {
            responseMap = getDocument(response.getRows().get(Math.toIntExact(response.getTotalRows()) -1).getId());
        }

        return responseMap;
    }


    public Map<String, Object> randomTaggedDocument() {

        Map<String, Object> responseMap = new HashMap<>();

        PostSearchOptions searchOptions = new PostSearchOptions.Builder()
            .db(DATABASE)
            .ddoc("allimages")
            .index("activeImages")
            .query("status:tagged")
            .build();

        SearchResult response =
            cloudantService.postSearch(searchOptions).execute()
                .getResult();

        if(response.getTotalRows() > 0) {
            int r = ThreadLocalRandom.current().nextInt(0, Math.toIntExact(response.getTotalRows()));
            responseMap = getDocument(response.getRows().get(r).getId());
        }

        return responseMap;
    }


    public Map<String, Object> randomUnTaggedDocument() {

        Map<String, Object> responseMap = new HashMap<>();

        PostSearchOptions searchOptions = new PostSearchOptions.Builder()
            .db(DATABASE)
            .ddoc("allimages")
            .index("activeImages")
            .query("status:new")
            .build();

        SearchResult response =
            cloudantService.postSearch(searchOptions).execute()
                .getResult();

        if(response.getTotalRows() > 0) {
            int r = ThreadLocalRandom.current().nextInt(0, Math.toIntExact(response.getTotalRows()));
            responseMap = getDocument(response.getRows().get(r).getId());
        }

        return responseMap;
    }

}
