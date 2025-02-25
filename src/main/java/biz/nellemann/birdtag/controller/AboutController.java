package biz.nellemann.birdtag.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.views.View;

@Controller("/about")
public class AboutController {

    @Get(uri = "/")
    @View("about/index.html")
    public HttpResponse<?> index() {
        return HttpResponse.ok();
    }

}
