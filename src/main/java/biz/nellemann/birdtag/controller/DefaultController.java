package biz.nellemann.birdtag.controller;


import biz.nellemann.birdtag.service.CloudantDataService;
import biz.nellemann.birdtag.service.ObjectStorageService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller("/")
public class DefaultController {

    private static final Logger log = LoggerFactory.getLogger(DefaultController.class);

    @Inject
    CloudantDataService cloudantDataService;

    @Inject
    ObjectStorageService objectStorageService;


    @View("index.html")
    @Get(uri = "/")
    public HttpResponse<?> index() {
        return HttpResponse.ok();
    }


    @Get(uri = "/test")
    public HttpResponse<?> test() {
        cloudantDataService.test();
        objectStorageService.test();
        return HttpResponse.ok();
    }



}
