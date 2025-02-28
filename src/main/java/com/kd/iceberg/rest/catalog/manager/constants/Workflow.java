package com.kd.iceberg.rest.catalog.manager.constants;

public enum Workflow {

    UPDATE("UPDATE"),
    ROLLBACK("ROLLBACK");

    private final String value;

    Workflow(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
