package biz.nellemann.birdtag;

import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import jakarta.inject.Singleton;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;

@Singleton
public class TemplateListener implements BeanCreatedEventListener<TemplateEngine> {

    @Override
    public TemplateEngine onCreated(BeanCreatedEvent<TemplateEngine> event) {
        TemplateEngine builder = event.getBean();
        builder.addDialect(new LayoutDialect());
        return builder;
    }
}

