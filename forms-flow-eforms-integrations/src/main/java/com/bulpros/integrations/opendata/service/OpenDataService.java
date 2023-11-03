package com.bulpros.integrations.opendata.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component("openDataService")
@RequiredArgsConstructor
@Slf4j
public class OpenDataService {

    private final RestTemplate restTemplate;

    @Value("${com.bulpros.opendata.url}")
    private String openDataUrl;
    @Value("${com.bulpros.opendata.api.key}")
    private String openDataApiKey;
    @Value("${com.bulpros.opendata.dataset.uri}")
    private String openDataDatasetUri;
    @Value("${com.bulpros.redash.url}")
    private String redashUrl;
    @Value("${com.bulpros.redash.api.key}")
    private String redashApiKey;
    @Value("${com.bulpros.opendata.redash.query.ids}")
    private final List<String> redashQueries;

    public String createResource() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        for (String queryId : redashQueries) {
            UriComponentsBuilder queryInfoUrl = UriComponentsBuilder.fromHttpUrl(redashUrl)
                    .path("api/queries/").path(queryId)
                    .queryParam("api_key", redashApiKey);
            JsonNode queryInfoResponse = restTemplate.getForObject(queryInfoUrl.toUriString(),
                    JsonNode.class);
            if (queryInfoResponse != null) {
                DocumentContext responseContext = JsonPath.using(Configuration.builder()
                        .options(Option.DEFAULT_PATH_LEAF_TO_NULL, Option.SUPPRESS_EXCEPTIONS)
                        .build()).parse(queryInfoResponse.toString());
                String queryName = responseContext.read("$.name");
                JSONArray queryColumns = responseContext.read("$.visualizations.[*].options.columns.[*].title");
                ObjectNode data = objectMapper.createObjectNode();
                ArrayNode dataHeaders = objectMapper.createArrayNode();
                if (!queryColumns.isEmpty()) {
                    String[] columns = new ObjectMapper().readValue(queryColumns.toString(), String[].class);
                    Arrays.stream(columns).forEach(dataHeaders::add);
                    data.set("headers", dataHeaders);
                }
                UriComponentsBuilder queryDataUrl = UriComponentsBuilder.fromHttpUrl(redashUrl)
                        .path("api/queries/").path(queryId).path("/results")
                        .queryParam("api_key", redashApiKey);
                ObjectNode queryDataParams = objectMapper.createObjectNode();
                queryDataParams.put("max_age", 0);
                HttpEntity<String> queryDataRequest =
                        new HttpEntity<>(objectMapper.writeValueAsString(queryDataParams), headers);
                JsonNode queryDataResponse = restTemplate.postForObject(queryDataUrl.toUriString(), queryDataRequest,
                        JsonNode.class);
                if (queryDataResponse != null) {
                    String jobId = queryDataResponse.get("job").get("id").asText();
                    boolean ready = false;
                    String queryResultId = null;
                    while (!ready) {
                        UriComponentsBuilder jobInfoUrl = UriComponentsBuilder.fromHttpUrl(redashUrl)
                                .path("api/jobs/").path(jobId)
                                .queryParam("api_key", redashApiKey);
                        JsonNode jobInfoResponse = restTemplate.getForObject(jobInfoUrl.toUriString(),
                                JsonNode.class);
                        if (jobInfoResponse != null) {
                            int status = jobInfoResponse.get("job").get("status").asInt();
                            if (status == 3) {
                                queryResultId = jobInfoResponse.get("job").get("query_result_id").asText();
                                ready = true;
                            } else ready = status != 1 && status != 2;
                        } else {
                            ready = true;
                        }
                    }
                    if (queryResultId != null) {
                        UriComponentsBuilder queryResultUrl = UriComponentsBuilder.fromHttpUrl(redashUrl)
                                .path("api/query_results/").path(queryResultId)
                                .queryParam("api_key", redashApiKey);
                        JsonNode queryResultResponse = restTemplate.getForObject(queryResultUrl.toUriString(),
                                JsonNode.class);
                        if (queryResultResponse != null) {
                            ArrayNode queryResultRows = (ArrayNode) queryResultResponse.get("query_result").get("data").get("rows");
                            int i = 0;
                            for (JsonNode queryResultRow : queryResultRows) {
                                ArrayNode dataRows = objectMapper.createArrayNode();
                                Iterator<Map.Entry<String, JsonNode>> fieldsIterator = queryResultRow.fields();
                                while (fieldsIterator.hasNext()) {
                                    Map.Entry<String, JsonNode> field = fieldsIterator.next();
                                    dataRows.add(field.getValue());
                                }
                                data.set("row" + ++i, dataRows);
                            }

                            UriComponentsBuilder addResourceMetadataUrl = UriComponentsBuilder.fromHttpUrl(this.openDataUrl)
                                    .path("api/")
                                    .path("addResourceMetadata");
                            ObjectNode resourceMetadata = objectMapper.createObjectNode();
                            resourceMetadata.put("api_key", openDataApiKey);
                            resourceMetadata.put("dataset_uri", openDataDatasetUri);
                            ObjectNode resourceMetadataData = objectMapper.createObjectNode();
                            resourceMetadataData.put("name", queryName + "( " + LocalDate.now().toString() + " )");
                            resourceMetadataData.put("locale", "bg");
                            resourceMetadataData.put("type", 1);
                            resourceMetadataData.put("file_format", "CSV");
                            resourceMetadata.set("data", resourceMetadataData);
                            HttpEntity<String> createResourceRequest =
                                    new HttpEntity<>(objectMapper.writeValueAsString(resourceMetadata), headers);

                            JsonNode resourceMetadataDataResponse = restTemplate.postForObject(addResourceMetadataUrl.toUriString(),
                                    createResourceRequest, JsonNode.class);

                            if (resourceMetadataDataResponse != null) {
                                String resourceUri = resourceMetadataDataResponse.get("data").get("uri").textValue();
                                UriComponentsBuilder addResourceDataUrl = UriComponentsBuilder.fromHttpUrl(this.openDataUrl)
                                        .path("api/")
                                        .path("addResourceData");
                                ObjectNode resourceData = objectMapper.createObjectNode();
                                resourceData.put("api_key", openDataApiKey);
                                resourceData.put("resource_uri", resourceUri);
                                resourceData.put("extension_format", "csv");
                                resourceData.set("data", data);
                                HttpEntity<String> populateResourceRequest =
                                        new HttpEntity<>(objectMapper.writeValueAsString(resourceData), headers);
                                restTemplate.postForObject(addResourceDataUrl.toUriString(),
                                        populateResourceRequest, JsonNode.class);
                            }

                        }
                    }
                }
            }
        }

        log.info("OpenData Submission Successful");

        return "";

    }

}
