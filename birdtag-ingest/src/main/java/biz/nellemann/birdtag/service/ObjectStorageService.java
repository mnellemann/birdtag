package biz.nellemann.birdtag.service;

import com.ibm.cloud.objectstorage.services.s3.model.*;
import io.micronaut.context.annotation.Property;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.SDKGlobalConfiguration;
import com.ibm.cloud.objectstorage.SdkClientException;
import com.ibm.cloud.objectstorage.auth.AWSCredentials;
import com.ibm.cloud.objectstorage.auth.AWSStaticCredentialsProvider;
import com.ibm.cloud.objectstorage.client.builder.AwsClientBuilder;
import com.ibm.cloud.objectstorage.oauth.BasicIBMOAuthCredentials;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Singleton
public class ObjectStorageService {

    private static final Logger log = LoggerFactory.getLogger(ObjectStorageService.class);

    private final static String COS_AUTH_ENDPOINT = "https://iam.cloud.ibm.com/identity/token";


    @Property(name = "cos.api.key")
    protected String COS_API_KEY_ID;

    @Property(name = "cos.service.crn")
    protected String COS_SERVICE_CRN;

    @Property(name = "cos.bucket.name")
    protected String COS_BUCKET_NAME;

    @Property(name = "cos.bucket.location")
    protected String COS_BUCKET_LOCATION;

    @Property(name = "cos.endpoint")
    protected String COS_ENDPOINT;


    private AmazonS3 cosClient;


    @PostConstruct
    public void init() {
        SDKGlobalConfiguration.IAM_ENDPOINT = COS_AUTH_ENDPOINT;

        try {
            cosClient = createClient(COS_API_KEY_ID, COS_SERVICE_CRN, COS_ENDPOINT, COS_BUCKET_LOCATION);
        } catch (SdkClientException sdke) {
            log.error("SDK Error: {}", sdke.getMessage());
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }

    public void test() {
        //listBuckets(cosClient);
        listObjects(COS_BUCKET_NAME);
        createTextFile("testFile.txt", "Content of text file in COS created at " + new Date());
        String foo = readTextFile("testFile.txt");
        log.info("We got the following text: {}", foo);
    }


    public static AmazonS3 createClient(String apiKey, String serviceInstanceId, String endpointUrl, String location)
    {
        AWSCredentials credentials = new BasicIBMOAuthCredentials(apiKey, serviceInstanceId);
        ClientConfiguration clientConfig = new ClientConfiguration()
            .withRequestTimeout(5000)
            .withTcpKeepAlive(true);

        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(credentials))
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpointUrl, location))
            .withPathStyleAccessEnabled(true)
            .withClientConfiguration(clientConfig)
            .build();
    }


    public void listObjects(String bucketName)
    {
        System.out.println("Listing objects in bucket " + bucketName);
        ObjectListing objectListing = cosClient.listObjects(new ListObjectsRequest().withBucketName(bucketName));
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + "  " + "(size = " + objectSummary.getSize() + ")");
        }
        System.out.println();
    }


    public void createBucket(String bucketName) {
        cosClient.createBucket(bucketName);
        log.info("Bucket: {} created", bucketName);
    }


    public void createTextFile(String itemName, String fileText) {

        byte[] arr = fileText.getBytes(StandardCharsets.UTF_8);
        InputStream newStream = new ByteArrayInputStream(arr);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(arr.length);
        metadata.setContentType("text/plain");

        PutObjectRequest req = new PutObjectRequest(COS_BUCKET_NAME, itemName, newStream, metadata);
        cosClient.putObject(req);

        log.info("Item: {} created!", itemName);
    }


    public String readTextFile(String itemName) {

        GetObjectRequest req = new GetObjectRequest(COS_BUCKET_NAME, itemName);
        S3Object object = cosClient.getObject(req);

        String output = "";
        try {
            output = new String(object.getObjectContent().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("readTextFile Error: {}", e.getMessage());
        }
        return output;
    }


    public void createBinaryFile(String itemName, String contentType, byte[] data) {

        InputStream newStream = new ByteArrayInputStream(data);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(data.length);
        metadata.setContentType(contentType);

        PutObjectRequest req = new PutObjectRequest(COS_BUCKET_NAME, itemName, newStream, metadata);
        PutObjectResult res = cosClient.putObject(req);

        log.info("Item: {} created!", itemName);
    }


}
