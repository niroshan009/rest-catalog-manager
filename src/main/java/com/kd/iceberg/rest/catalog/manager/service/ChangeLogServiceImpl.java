package com.kd.iceberg.rest.catalog.manager.service;

import com.kd.iceberg.rest.catalog.manager.entity.ChangeLog;
import com.kd.iceberg.rest.catalog.manager.model.ChangesModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class ChangeLogServiceImpl implements ChangeLogService {


    private ChangesModel changesModel;

    public ChangeLogServiceImpl(ChangesModel changesModel) {
        this.changesModel = changesModel;
    }
    @Override
    public void executeChangeLog() {

        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        Resource resource = new ClassPathResource("changelog-master.yaml");


        System.out.println("=====");



    }
}
