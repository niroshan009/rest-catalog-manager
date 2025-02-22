package com.kd.iceberg.rest.catalog.manager.model.iceberg.table.create;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTableReqeust implements Serializable {

    @JsonProperty("name")
    private String name;

    @JsonProperty("schema")
    private Schema schema;

    @JsonProperty("location")
    private String location;

    @JsonProperty("partition-spec")
    private PartitionSpec partitionSpec;

    @JsonProperty("write-order")
    private WriteOrder writeOrder;

    @JsonProperty("stage-create")
    private boolean stageCreate;

    @JsonProperty("properties")
    private Properties properties;
}
