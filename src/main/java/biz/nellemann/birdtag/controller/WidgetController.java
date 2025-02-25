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

import java.util.Map;

@Controller("/widget")
public class WidgetController {


    private static final Logger log = LoggerFactory.getLogger(WidgetController.class);

    @Inject
    CloudantDataService cloudantDataService;

    @Inject
    ObjectStorageService objectStorageService;


    @Get(uri = "/latest")
    @View("widget/image.html")
    public HttpResponse<Map<?,?>> latest() {
        Map<String, Object> model = cloudantDataService.latestDocument();
        model.put("title", "Latest Bird Image");
        log.info("latest() - {}", model.toString());
        return HttpResponse.ok(model);
    }


    @View("widget/image.html")
    @Get(uri = "/untagged")
    public HttpResponse<Map<?,?>> untagged() {
        Map<String, Object> model = cloudantDataService.latestDocument();
        model.put("title", "Random Untagged Image");
        log.info("untagged() - {}", model.toString());
        return HttpResponse.ok(model);
    }


    @View("widget/image.html")
    @Get(uri = "/tagged")
    public HttpResponse<Map<?,?>> tagged() {
        Map<String, Object> model = cloudantDataService.latestDocument();
        model.put("title", "Random Tagged Image");
        log.info("tagged() - {}", model.toString());
        return HttpResponse.ok(model);    }
}
