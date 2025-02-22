package com.kd.iceberg.rest.catalog.manager.model.iceberg.table.create;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Properties {

    @JsonProperty("format-version")
    private String formatVersion;

}
