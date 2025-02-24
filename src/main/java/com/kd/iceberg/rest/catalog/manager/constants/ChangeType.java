package com.kd.iceberg.rest.catalog.manager.constants;

public enum ChangeType {

    CREATE("CREATE"),
    DROP("DROP");

    private final String value;

    ChangeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
