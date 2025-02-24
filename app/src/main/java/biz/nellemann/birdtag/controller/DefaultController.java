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
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller("/")
public class DefaultController {

    private static final Logger log = LoggerFactory.getLogger(DefaultController.class);

    @Inject
    CloudantDataService cloudantDataService;

    @Inject
    ObjectStorageService objectStorageService;


    @Get(uri = "/")
    public HttpResponse<?> index() {
        cloudantDataService.test();
        objectStorageService.test();
        return HttpResponse.ok();
    }

    @Post(value = "/ingest", consumes = { MediaType.MULTIPART_FORM_DATA },  produces = MediaType.TEXT_HTML )
    public HttpResponse<?> upload(@Part CompletedFileUpload image, String station) throws IOException {
        log.info("ingest() - image={}, station={}", image.getFilename(), station);

        UUID uuid = UUID.randomUUID();
        ZonedDateTime now = ZonedDateTime.now();

        String image_path = String.format("%s/%d/%d/%d/%s",
            station,
            now.getYear(),
            now.getMonthValue(),
            now.getDayOfMonth(),
            getFileExtension(uuid.toString(), image.getFilename())
        );

        Map<String, Object> properties = new HashMap<>();
        properties.put("created_at", now.toString());
        properties.put("image_path", image_path);
        properties.put("station", station);
        properties.put("status", "new");

        try {
            objectStorageService.createBinaryFile(image_path, String.valueOf(image.getContentType()), image.getBytes());
            String documentId = cloudantDataService.createDocument(properties);
            return HttpResponse.ok("OK.");
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
