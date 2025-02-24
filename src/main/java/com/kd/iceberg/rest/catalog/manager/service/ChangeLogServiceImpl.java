package com.kd.iceberg.rest.catalog.manager.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.iceberg.rest.catalog.manager.constants.ChangeType;
import com.kd.iceberg.rest.catalog.manager.entity.ChangeLog;
import com.kd.iceberg.rest.catalog.manager.exception.IcebergTableDDLException;
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

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.*;


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

        Stack<ChangeLog> changeLogStack = new Stack<>();


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
                    try {HttpResponse r = HttpClient.newBuilder()
                                .build()
                                .send(request, HttpResponse.BodyHandlers.ofString());
                        System.out.println(r.statusCode());

                        if(r.statusCode() != 200){
                            log.error("Error while creating table: {}", r.body().toString());
                            throw new IcebergTableDDLException(changeLogStack, r.body().toString());
                        }
                    } catch (IOException | InterruptedException e) {
                        log.error("Error while creating table: {}", e.getMessage());
                        throw new IcebergTableDDLException(changeLogStack, e.getMessage());
                    }

                    changeLogStack.push(ChangeLog.builder()
                            .changeLogName(changes.getName())
                            .changeType(ChangeType.CREATE)
                            .icebergTable(changes.getTable())
                            .author(changes.getAuthor())
                            .icebergNamespace(changes.getNamespace())
                            .changeDescription(changes.getDescription())
                            .build());

                }
                case "drop" -> {

                    log.info("Dropping table: {}", changes.getTable());

                    if(null == changes.getRollbackStruct()){
                        throw new RuntimeException("Rollback structure is missing for drop action");
                    }


                    String url = String.format("http://localhost:8181/v1/namespaces/%s/tables/%s?purgeRequested=true",changes.getNamespace(), changes.getTable());
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(java.net.URI.create(url))
                            .header("Content-Type", "application/json")
                            .DELETE()
                            .build();
                   try {
                       HttpClient.newBuilder()
                               .build()
                               .send(request, HttpResponse.BodyHandlers.ofString());
                   } catch (IOException | InterruptedException e) {
                       log.error("Error while dropping table: {}", e.getMessage());
                       throw new IcebergTableDDLException(changeLogStack, e.getMessage());
                   }

                    changeLogStack.push(ChangeLog.builder()
                            .changeLogName(changes.getName())
                            .changeType(ChangeType.DROP)
                            .icebergTable(changes.getTable())
                            .author(changes.getAuthor())
                            .rollbackStruct(changes.getRollbackStruct())
                            .icebergNamespace(changes.getNamespace())
                            .changeDescription(changes.getDescription())
                            .build());
                }


            }

        }


        System.out.println("=====");


    }
}
