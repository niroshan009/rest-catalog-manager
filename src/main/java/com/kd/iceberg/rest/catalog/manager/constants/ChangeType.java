package com.kd.iceberg.rest.catalog.manager.constants;

public enum ChangeType {

    CREATE("CREATE"),
    DELETE("DELETE");

    private String value;

    ChangeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
