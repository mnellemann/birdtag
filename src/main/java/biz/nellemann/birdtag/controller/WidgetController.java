package biz.nellemann.birdtag.controller;

import biz.nellemann.birdtag.service.CloudantDataService;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.views.View;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
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
        log.debug("latest() - {}", model);
        return HttpResponse.ok(model);
    }


    @View("widget/image.html")
    @Get(uri = "/untagged")
    public HttpResponse<Map<?,?>> untagged() {
        Map<String, Object> model = cloudantDataService.randomUnTaggedDocument();
        model.put("title", "Random UntaggedImage");
        log.debug("untagged() - {}", model);
        return HttpResponse.ok(model);
    }


    @View("widget/image.html")
    @Get(uri = "/tagged")
    public HttpResponse<Map<?,?>> tagged() {
        Map<String, Object> model = cloudantDataService.randomTaggedDocument();
        model.put("title", "Random Tagged Image");
        log.debug("tagged() - {}", model);
        return HttpResponse.ok(model);
    }


    @Post(value = "/tag/{id}", consumes = { MediaType.ALL },  produces = MediaType.TEXT_HTML )
    public HttpResponse<?> tag(HttpHeaders headers, String id) {
        //headers.forEach(header -> log.info("{} = {}", header.getKey(), header.getValue()));
        String prompt = headers.get("HX-Prompt");
        log.info("ID: {}, Prompt was: {}", id, prompt);
        cloudantDataService.updateDocument(id, "species", prompt);
        return HttpResponse.ok("Saved").header("HX-Refresh", "true");
    }

}
