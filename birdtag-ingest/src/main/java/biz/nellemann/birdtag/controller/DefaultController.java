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

    @Post(value = "/upload", consumes = { MediaType.MULTIPART_FORM_DATA },  produces = MediaType.TEXT_HTML )
    public HttpResponse<?> upload(@Part CompletedFileUpload file) throws IOException {
        log.info("upload() - file={}", file.getFilename());

        // TODO: Save to COS
        //FileOutputStream fout = new FileOutputStream("/tmp/dnc-update.zip");
        //fout.write(file.getBytes());
        //fout.close();

        return HttpResponse.ok("OK.");
        //return HttpResponse.badRequest("Error.");

    }

}
