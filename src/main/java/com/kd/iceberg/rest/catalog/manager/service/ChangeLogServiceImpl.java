package com.kd.iceberg.rest.catalog.manager.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.iceberg.rest.catalog.manager.constants.ChangeType;
import com.kd.iceberg.rest.catalog.manager.constants.Workflow;
import com.kd.iceberg.rest.catalog.manager.entity.ChangeLog;
import com.kd.iceberg.rest.catalog.manager.exception.IcebergTableDDLException;
import com.kd.iceberg.rest.catalog.manager.model.iceberg.table.create.CreateTableReqeust;


import com.kd.iceberg.rest.catalog.manager.model.Changes;
import com.kd.iceberg.rest.catalog.manager.properties.CatalogProperties;
import com.kd.iceberg.rest.catalog.manager.repository.ChangeLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    public void executeChangeLog(Workflow workflow, String tag) throws IOException, InterruptedException {



        switch (workflow) {
            case UPDATE -> update(tag);
            case ROLLBACK -> rollback(tag);
        }



        System.out.println("=====");
    }

    private void rollback(String tag) {
    }


    private void update(String tag) throws IOException, InterruptedException {
        Stack<ChangeLog> changeLogStack = new Stack<>();
        List<ChangeLog> changeLogs = changeLogRepository.findAll();

        List<Changes> nonExistingChangeLog =  catalogProperties.getChanges()
                .stream()
                .filter(e-> !changeLogs.stream().anyMatch(ex -> ex.getChangeLogName().equals(e.getName())))
                .toList();

        for (Changes changes : nonExistingChangeLog) {
            ChangeLog changeLog = new ChangeLog();
            changeLog.setChangeLogName(changes.getName());
            changeLog.setIcebergTable(changes.getTable());
            changeLog.setAuthor(changes.getAuthor());
            changeLog.setIcebergNamespace(changes.getNamespace());
            changeLog.setChangeDescription(changes.getDescription());
            changeLog.setTableStruct(changes.getStruct());
            changeLog.setTag(tag);

            switch (changes.getAction()) {
                case "create" -> {
                    try {
                        ChangeLog createChangeLog = createChangeLog(changes, changeLog);
                        changeLogStack.push(createChangeLog);
                    } catch (IcebergTableDDLException icebergTableDDLException) {
                        throw new IcebergTableDDLException(changeLogStack, icebergTableDDLException.getMessage());
                    }
                }
                case "drop" -> {
                    try{
                        ChangeLog dropChangeLog = dropTable(changes, changeLog);
                        changeLogStack.push(dropChangeLog);
                    } catch (IcebergTableDDLException icebergTableDDLException) {
                        throw new IcebergTableDDLException(changeLogStack, icebergTableDDLException.getMessage());
                    }
                }
            }
        }

        changeLogRepository.saveAll(changeLogStack);
    }


    private ChangeLog dropTable(Changes changes, ChangeLog changeLog) throws IcebergTableDDLException {
        log.info("Dropping table: {}", changes.getTable());

        if(null == changes.getRollbackStruct()){
            throw new RuntimeException("Rollback structure is missing for drop action");
        }


        String url = String.format("%s1/namespaces/%s/tables/%s?purgeRequested=true",icebergEndpoint,
                changes.getNamespace(), changes.getTable());
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
            throw new IcebergTableDDLException(e.getMessage());
        }

        changeLog.setChangeType(ChangeType.DROP);
        return changeLog;
    }

    private ChangeLog createChangeLog(Changes changes, ChangeLog changeLog) throws IOException, IcebergTableDDLException {
        log.info("Creating table: {}", changes.getTable());
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String tableStructure = StreamUtils.copyToString(new ClassPathResource(changes.getStruct()).getInputStream(), Charset.defaultCharset());
        log.info(tableStructure);
        CreateTableReqeust s = mapper.readValue(tableStructure, CreateTableReqeust.class);
        String namespace = changes.getNamespace();
        String url = String.format("%s/namespaces/%s/tables", icebergEndpoint, namespace);
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
                throw new IcebergTableDDLException( r.body().toString());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error while creating table: {}", e.getMessage());
            throw new IcebergTableDDLException( e.getMessage());
        }

        changeLog.setChangeType(ChangeType.CREATE);
        return changeLog;
    }
}
