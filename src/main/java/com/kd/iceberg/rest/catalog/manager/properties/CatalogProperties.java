package com.kd.iceberg.rest.catalog.manager.properties;

import com.kd.iceberg.rest.catalog.manager.model.Changes;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "catalog")
public class CatalogProperties {

    private  List<Changes> changes;

}
