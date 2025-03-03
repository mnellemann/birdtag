package biz.nellemann.birdtag.controller;

import biz.nellemann.birdtag.service.CloudantDataService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.views.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Controller("/tag")
public class TagController {

    private static final Logger log = LoggerFactory.getLogger(TagController.class);

    @Inject
    CloudantDataService cloudantDataService;


    @Get(uri = "/")
    @View("tag/index.html")
    public HttpResponse<?> index() {
        return HttpResponse.ok();
    }


    @Get(uri = "/list")
    @View("tag/list.html")
    public HttpResponse<?> list() {
        log.info("list()");
        Map<String, Object> model = new HashMap<>();
        model.put("tags", cloudantDataService.find("tag:"));
        log.info("list(): {}", model);
        return HttpResponse.ok(model);
    }


    @Post(value = "/add", consumes = { MediaType.ALL },  produces = MediaType.TEXT_HTML )
    public HttpResponse<?> add(String tag) {
        //headers.forEach(header -> log.info("{} = {}", header.getKey(), header.getValue()));

        String escapedTag = convertUtf8ToAscii(tag);
        ZonedDateTime now = ZonedDateTime.now();

        Map<String, Object> properties = new HashMap<>();
        properties.put("_id", String.format("tag:%s", escapedTag));
        properties.put("timestamp", now.toString());
        properties.put("name", tag);
        properties.put("active", true);

        cloudantDataService.createDocument(properties);
        return HttpResponse.ok("Saved").header("HX-Refresh", "true");
    }


    public static String convertUtf8ToAscii(String utf8String) {
        utf8String = utf8String.strip();
        utf8String = utf8String.toLowerCase(Locale.ROOT);
        StringBuilder asciiString = new StringBuilder();
        for (char c : utf8String.toCharArray()) {
            if (c < 128) {
                asciiString.append(c);
            } else {
                // Replace with a question mark for non-ASCII characters
                //asciiString.append('');
            }
        }
        return asciiString.toString();
    }
}
