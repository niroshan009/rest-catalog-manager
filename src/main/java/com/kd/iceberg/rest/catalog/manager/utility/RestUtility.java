package com.kd.iceberg.rest.catalog.manager.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;

@Slf4j
public class RestUtility {


    public static HttpResponse<String> createTable(String url, String tableStructure) throws IOException, InterruptedException {

        String tableSctuct = StreamUtils.copyToString(new ClassPathResource(tableStructure).getInputStream(), Charset.defaultCharset());
        log.info("Creating table with structure: {}", tableSctuct);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(tableSctuct))
                .build();
        return HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }


    public static HttpResponse<String> dropTable(String url) throws IOException, InterruptedException {
        log.info("Dropping table: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
       return HttpClient.newBuilder()
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }
}
