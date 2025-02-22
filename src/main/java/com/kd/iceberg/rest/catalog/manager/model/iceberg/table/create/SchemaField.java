package com.kd.iceberg.rest.catalog.manager.model.iceberg.table.create;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SchemaField implements Serializable {

    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private String type;

    @JsonProperty("required")
    private boolean required;

    @JsonProperty("doc")
    private String doc;
}
