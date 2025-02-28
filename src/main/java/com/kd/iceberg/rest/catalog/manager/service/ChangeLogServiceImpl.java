package com.kd.iceberg.rest.catalog.manager.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kd.iceberg.rest.catalog.manager.constants.ChangeType;
import com.kd.iceberg.rest.catalog.manager.constants.Workflow;
import com.kd.iceberg.rest.catalog.manager.entity.ChangeLog;
import com.kd.iceberg.rest.catalog.manager.exception.IcebergTableDDLException;
import com.kd.iceberg.rest.catalog.manager.model.Changes;
import com.kd.iceberg.rest.catalog.manager.model.iceberg.table.create.CreateTableReqeust;
import com.kd.iceberg.rest.catalog.manager.properties.CatalogProperties;
import com.kd.iceberg.rest.catalog.manager.repository.ChangeLogRepository;
import com.kd.iceberg.rest.catalog.manager.utility.RestUtility;
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
import java.util.List;
import java.util.Stack;


@Slf4j
@Service
public class ChangeLogServiceImpl implements ChangeLogService {


    private final CatalogProperties catalogProperties;

    private final ChangeLogRepository changeLogRepository;

    @Value("${iceberg.endpoint}")
    private String icebergEndpoint;


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

        log.info("Change log executed successfully");
    }

    private void rollback(String tag) {
        Stack<ChangeLog> changeLogStack = changeLogRepository.findLatestRecordsByTagIdToRollback(tag);

        for (ChangeLog changeLog : changeLogStack) {

            switch (changeLog.getChangeType()) {
                case CREATE -> {
                    try {
                        catalogProperties.getChanges().stream().filter(e -> e.getName().equals(changeLog.getChangeLogName())).findFirst().ifPresent(e -> {
                            try {
                                dropTable(e, changeLog);
                            } catch (IcebergTableDDLException icebergTableDDLException) {
                                icebergTableDDLException.printStackTrace();
                            }
                        });

                    } catch (IcebergTableDDLException icebergTableDDLException) {
                        icebergTableDDLException.printStackTrace();
                    }
                }
                case DROP -> {
                    catalogProperties.getChanges().stream().filter(e -> e.getName().equals(changeLog.getChangeLogName())).findFirst().ifPresent(e -> {
                        try {
                            createChangeLog(e, changeLog);
                        } catch (IcebergTableDDLException icebergTableDDLException) {
                            icebergTableDDLException.printStackTrace();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                }
            }
        }

        changeLogRepository.deleteAll(changeLogStack);

        log.info("Rollback completed successfully");
    }


    private void update(String tag) throws IOException, InterruptedException {
        Stack<ChangeLog> changeLogStack = new Stack<>();
        Stack<ChangeLog> existingTags = changeLogRepository.findAllByTagOrderByIdAsc(tag);
        if (!existingTags.isEmpty()) {
            throw new RuntimeException("Existing tag found. Please rollback the existing tag before updating");
        }
        List<ChangeLog> changeLogs = changeLogRepository.findAll();

        List<Changes> nonExistingChangeLog = catalogProperties.getChanges()
                .stream()
                .filter(e -> !changeLogs.stream().anyMatch(ex -> ex.getChangeLogName().equals(e.getName())))
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
                    try {
                        ChangeLog dropChangeLog = dropTable(changes, changeLog);
                        changeLogStack.push(dropChangeLog);
                    } catch (IcebergTableDDLException icebergTableDDLException) {
                        throw new IcebergTableDDLException(changeLogStack, icebergTableDDLException.getMessage());
                    }
                }
            }
        }

        changeLogRepository.saveAll(changeLogStack);
        log.info("Change log updated successfully");
    }


    private ChangeLog dropTable(Changes changes, ChangeLog changeLog) throws IcebergTableDDLException {
        log.info("Dropping table: {}", changes.getTable());

        if (null == changes.getRollbackStruct()) {
            throw new RuntimeException("Rollback structure is missing for drop action");
        }


        String url = String.format("%s/namespaces/%s/tables/%s?purgeRequested=true", icebergEndpoint,
                changes.getNamespace(), changes.getTable());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
        try {RestUtility.dropTable(url);
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

        try {HttpResponse<String> r = RestUtility.createTable(url, changes.getStruct());

            if (r.statusCode() != 200) {
                log.error("Error while creating table: {}", r.body());
                throw new IcebergTableDDLException(r.body());
            }
        } catch (IOException | InterruptedException e) {
            log.error("Error while creating table: {}", e.getMessage());
            throw new IcebergTableDDLException(e.getMessage());
        }

        changeLog.setChangeType(ChangeType.CREATE);
        return changeLog;
    }
}
