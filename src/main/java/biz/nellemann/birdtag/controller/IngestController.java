package biz.nellemann.birdtag.controller;

import biz.nellemann.birdtag.service.CloudantDataService;
import biz.nellemann.birdtag.service.ObjectStorageService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Part;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.views.View;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller("/ingest")
public class IngestController {


    private static final Logger log = LoggerFactory.getLogger(IngestController.class);

    @Inject
    CloudantDataService cloudantDataService;

    @Inject
    ObjectStorageService objectStorageService;



    @View("ingest/index.html")
    @Get(uri = "/")
    public HttpResponse<?> index() {
        //Map<String, String> model = new HashMap<>();
        //model.put("resolution", configService.contains("sensor.resolution") ? configService.get("sensor.resolution") : "1000");
        return HttpResponse.ok();
    }



    @Post(value = "/save", consumes = { MediaType.MULTIPART_FORM_DATA },  produces = MediaType.TEXT_HTML )
    public HttpResponse<?> upload(@Part CompletedFileUpload image, String station) throws IOException {
        log.info("save() - image={}, station={}", image.getFilename(), station);

        UUID uuid = UUID.randomUUID();
        ZonedDateTime now = ZonedDateTime.now();

        String image_path = String.format("%s/%d/%d/%d/%s",
            station,
            now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            getFileExtension(uuid.toString(), image.getFilename())
        );


        try {
            String objectUrl = objectStorageService.createBinaryFile(image_path, String.valueOf(image.getContentType()), image.getBytes());
            Map<String, Object> properties = new HashMap<>();
            properties.put("timestamp", now.toString());
            properties.put("path", image_path);
            properties.put("url", objectUrl);
            properties.put("station", station);
            properties.put("status", "new");
            properties.put("active", true);

            String documentId = cloudantDataService.createDocument(properties);
            log.info("save() - Uploaded file to {} and stored document as {}", objectUrl, documentId);
            return HttpResponse.ok("Saved " + documentId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return HttpResponse.badRequest("Error.");
        }

    }


    private String getFileExtension(String prefix, String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return prefix; // empty extension
        }
        return prefix+filename.substring(lastIndexOf);
    }
}
