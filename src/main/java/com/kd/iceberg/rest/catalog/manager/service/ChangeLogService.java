package com.kd.iceberg.rest.catalog.manager.service;

import com.kd.iceberg.rest.catalog.manager.constants.ChangeType;
import com.kd.iceberg.rest.catalog.manager.constants.Workflow;

import java.io.IOException;

public interface ChangeLogService {

    void executeChangeLog(Workflow workflow, String tag) throws IOException, InterruptedException;
}
