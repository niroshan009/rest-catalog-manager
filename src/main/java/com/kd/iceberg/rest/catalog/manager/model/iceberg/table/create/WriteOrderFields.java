package com.kd.iceberg.rest.catalog.manager.model.iceberg.table.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WriteOrderFields {

    @JsonProperty("source-id")
    private int sourceId;

    @JsonProperty("transform")
    private String transform;

    @JsonProperty("direction")
    private String direction;

    @JsonProperty("null-order")
    private String nullOrder;
}
