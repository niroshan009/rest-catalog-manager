package com.kd.iceberg.rest.catalog.manager.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.iceberg.rest.catalog.manager.model.iceberg.table.create.CreateTableReqeust;


import com.kd.iceberg.rest.catalog.manager.model.Changes;
import com.kd.iceberg.rest.catalog.manager.properties.CatalogProperties;
import com.kd.iceberg.rest.catalog.manager.repository.ChangeLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
@Service
public class ChangeLogServiceImpl implements ChangeLogService {


    private final CatalogProperties catalogProperties;

    private final ChangeLogRepository changeLogRepository;

    @Value("${iceberg.endpoint}")
    private String icebergEndpoint;

    @Value("${iceberg.api.version}")
    private String icebergApiVersion;



    public ChangeLogServiceImpl(CatalogProperties catalogProperties, ChangeLogRepository changeLogRepository) {
        this.catalogProperties = catalogProperties;
        this.changeLogRepository = changeLogRepository;
    }

    @Override
    public void executeChangeLog() throws IOException, InterruptedException {

        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();


        for (Changes changes : catalogProperties.getChanges()) {

            switch (changes.getAction()) {
                case "create" -> {
                    log.info("Creating table: {}", changes.getTable());
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    String tableStructure = StreamUtils.copyToString(new ClassPathResource(changes.getStruct()).getInputStream(), Charset.defaultCharset());
                    log.info(tableStructure);
                    CreateTableReqeust s = mapper.readValue(tableStructure, CreateTableReqeust.class);
                    String namespace = changes.getNamespace();
                    String url = String.format("http://localhost:8181/v1/namespaces/%s/tables", namespace);
                    log.info("URL: {}", url);
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(java.net.URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(tableStructure))
                            .build();
                    HttpResponse<String> response = HttpClient.newBuilder()
                            .build()
                            .send(request, HttpResponse.BodyHandlers.ofString());
                }
                case "drop" -> log.info("Dropping table: {}", changes.getTable());
                case "alter" -> log.info("Altering table: {}", changes.getTable());
            }

        }


        System.out.println("=====");


    }
}
