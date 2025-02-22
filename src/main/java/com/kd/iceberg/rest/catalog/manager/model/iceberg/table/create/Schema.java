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
public class Schema implements Serializable {

    @JsonProperty("type")
    private String type;

    @JsonProperty("fields")
    private List<SchemaField> fields;

    @JsonProperty("schema-id")
    private int schemaId;

    @JsonProperty("identifier-field-ids")
    private int[] identifierFieldIds;
}
