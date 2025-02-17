package com.kd.iceberg.rest.catalog.manager.model;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "changes")
public class ChangesModel {

    List<Changes> changes;


    private static class Changes{
        @Value("${change_log_name}")
        private String changeLogName;

        @Value("${change_description}")
        private String changeDescription;

        @Value("${author}")
        private String author;

        @Value("${table}")
        private String table;

        @Value("${namespace}")
        private String namespace;

        @Value("${change_struct}")
        private String changeStruct;
    }



}
