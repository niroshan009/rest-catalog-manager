package com.kd.iceberg.rest.catalog.manager;

import com.kd.iceberg.rest.catalog.manager.constants.ChangeType;
import com.kd.iceberg.rest.catalog.manager.constants.Workflow;
import com.kd.iceberg.rest.catalog.manager.service.ChangeLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class RestCatalogManagerApplication implements ApplicationRunner {

	public static void main(String[] args) {
		SpringApplication.run(RestCatalogManagerApplication.class, args);
	}


	@Autowired
	private ChangeLogService changeLogService;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		log.info("******* Starting to run the application *******");

		String changeType = args.getOptionValues("workflow").get(0);
		String tag = args.getOptionValues("tag").get(0);

		if(null == tag || tag.isEmpty()) {
			throw new IllegalArgumentException("Tag is required");
		}

		changeLogService.executeChangeLog(Workflow.valueOf(changeType), tag);

		log.info("******* Ending the application *******");
	}
}
