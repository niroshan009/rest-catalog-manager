package com.kd.iceberg.rest.catalog.manager.model.iceberg.table.create;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PartitionSpec {

    @JsonProperty("spec-id")
    private int specId;

    @JsonProperty("fields")
    private List<PartitionField> fields;

}
