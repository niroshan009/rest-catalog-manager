package com.kd.iceberg.rest.catalog.manager.exception;

import com.kd.iceberg.rest.catalog.manager.constants.ChangeType;
import com.kd.iceberg.rest.catalog.manager.entity.ChangeLog;
import com.kd.iceberg.rest.catalog.manager.utility.RestUtility;

import java.io.IOException;
import java.util.Stack;

public class IcebergTableDDLException extends RuntimeException {

    Stack<ChangeLog> stack = new Stack<>();

    public IcebergTableDDLException(Stack<ChangeLog> changeLog, String message) throws IOException, InterruptedException {
        super(message);

        while (!changeLog.isEmpty()) {
            ChangeLog changeLg = changeLog.pop();

            if(changeLg.getChangeType().equals(ChangeType.CREATE)) {
                String url = String
                        .format("http://localhost:8181/v1/namespaces/%s/tables/%s?purgeRequested=false",
                                changeLg.getIcebergNamespace(),
                                changeLg.getIcebergTable());

                RestUtility.dropTable(url);
            } else if(
                    changeLg.getChangeType().equals(ChangeType.DROP)) {
                String url = String.format("http://localhost:8181/v1/namespaces/%s/tables", changeLg.getIcebergNamespace());
                RestUtility
                        .createTable(url, changeLg.getRollbackStruct());
            }
        }
    }
    public IcebergTableDDLException(String message) {
        super(message);
    }

    public IcebergTableDDLException(String message, Throwable cause) {
        super(message, cause);
    }
}
