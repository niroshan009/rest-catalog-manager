package com.kd.iceberg.rest.catalog.manager;

import com.kd.iceberg.rest.catalog.manager.constants.ChangeType;
import com.kd.iceberg.rest.catalog.manager.constants.Workflow;
import com.kd.iceberg.rest.catalog.manager.service.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestCatalogManagerApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(RestCatalogManagerApplication.class, args);
	}


	@Autowired
	private ChangeLogService changeLogService;

//	@Override
//	public void run(String... args) throws Exception {
//		System.out.println("******* Starting to run the application *******");
//
//		changeLogService.executeChangeLog();
//
//
//
//		System.out.println("******* Ending the application *******");
//	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println("******* Starting to run the application *******");

		String changeType = args.getOptionValues("workflow").get(0);
		String tag = args.getOptionValues("tag").get(0);

		if(null == tag || tag.isEmpty()) {
			throw new IllegalArgumentException("Tag is required");
		}

		changeLogService.executeChangeLog(Workflow.valueOf(changeType), tag);

		System.out.println("******* Ending the application *******");
	}
}
