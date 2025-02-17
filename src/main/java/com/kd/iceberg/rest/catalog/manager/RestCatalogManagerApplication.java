package com.kd.iceberg.rest.catalog.manager;

import com.kd.iceberg.rest.catalog.manager.service.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

@SpringBootApplication
public class RestCatalogManagerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(RestCatalogManagerApplication.class, args);
	}


	@Autowired
	private ChangeLogService changeLogService;

	@Override
	public void run(String... args) throws Exception {
		System.out.println("******* Starting to run the application *******");

		changeLogService.executeChangeLog();



		System.out.println("******* Ending the application *******");
	}
}
