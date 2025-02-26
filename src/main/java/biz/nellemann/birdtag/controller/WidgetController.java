package biz.nellemann.birdtag.controller;

import biz.nellemann.birdtag.service.CloudantDataService;
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


    @Get(uri = "/latest")
    @View("widget/image.html")
    public HttpResponse<Map<?,?>> latest() {
        Map<String, Object> model = cloudantDataService.latestDocument();
        model.put("title", "Latest Image");
        log.info("latest() - {}", model);
        return HttpResponse.ok(model);
    }


    @View("widget/image.html")
    @Get(uri = "/untagged")
    public HttpResponse<Map<?,?>> untagged() {
        Map<String, Object> model = cloudantDataService.randomUnTaggedDocument();
        model.put("title", "Random UntaggedImage");
        log.info("untagged() - {}", model);
        return HttpResponse.ok(model);
    }


    @View("widget/image.html")
    @Get(uri = "/tagged")
    public HttpResponse<Map<?,?>> tagged() {
        Map<String, Object> model = cloudantDataService.randomTaggedDocument();
        model.put("title", "Random Tagged Image");
        log.info("tagged() - {}", model);
        return HttpResponse.ok(model);
    }

}
