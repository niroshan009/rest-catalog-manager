package com.kd.iceberg.rest.catalog.manager.model;


import lombok.Data;

@Data
public class Changes {

    private int id;

    private String name;

    private String description;

    private String author;

    private String table;

    private String namespace;

    private String action;

    private String struct;

    private String rollbackStruct;

}
