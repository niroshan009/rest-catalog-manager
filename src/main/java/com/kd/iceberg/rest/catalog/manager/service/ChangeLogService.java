package com.kd.iceberg.rest.catalog.manager.service;

import java.io.IOException;

public interface ChangeLogService {

    void executeChangeLog() throws IOException, InterruptedException;
}
