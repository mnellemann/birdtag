package biz.nellemann.birdtag.service;

import io.micronaut.management.endpoint.annotation.Endpoint;
import io.micronaut.management.endpoint.annotation.Read;
import io.micronaut.http.MediaType;
import jakarta.inject.Inject;

@Endpoint(id = "probe",
    prefix = "custom",
    defaultEnabled = true,
    defaultSensitive = false)
public class ProbeService {

    @Inject
    CloudantDataService cloudantDataService;


    @Read(produces = MediaType.TEXT_PLAIN)
    public String probe() {
        return cloudantDataService.test();
    }

}
