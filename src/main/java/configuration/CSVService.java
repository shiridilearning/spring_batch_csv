package configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

@Service
public class CSVService {

    @Autowired
    @Lazy
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    String uri = "http://localhost:8081/api/models/";

    public Vin getVinAndModel(Vin vin){
        String modelNo = restTemplate.getForObject(uri+"model/"+vin.getVinno(), String.class);
        vin.setModel(modelNo);
        return vin;
    }

}
