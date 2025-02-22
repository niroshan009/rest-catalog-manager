package com.kd.iceberg.rest.catalog.manager.model.iceberg.table.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class WriteOrder {

    @JsonProperty("order-id")
    private int orderId;

    @JsonProperty("fields")
    private List<WriteOrderFields> fields;
}
