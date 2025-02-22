package com.kd.iceberg.rest.catalog.manager.model.iceberg.table.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PartitionField {

    @JsonProperty("source-id")
    private int sourceId;

    @JsonProperty("transform")
    private String transform;

    @JsonProperty("name")
    private String name;

    @JsonProperty("field-id")
    private int fieldId;

}
