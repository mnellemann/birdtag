package biz.nellemann.birdtag.controller;

import biz.nellemann.birdtag.service.CloudantDataService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.Map;

@Controller("/browse")
public class BrowseController {

    private static final Logger log = LoggerFactory.getLogger(BrowseController.class);

    @Inject
    CloudantDataService cloudantDataService;


    @Get(uri = "/")
    @View("browse/index.html")
    public HttpResponse<?> index() {
        return HttpResponse.ok();
    }

    @Get(uri = "/list")
    @View("browse/list.html")
    public HttpResponse<?> list() {
        log.info("list()");
        Map<String, Object> model = new HashMap<>();
        model.put("images", cloudantDataService.find("image:"));
        log.info("list(): {}", model);
        return HttpResponse.ok(model);
    }
}


