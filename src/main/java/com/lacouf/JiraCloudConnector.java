package com.lacouf;

import lombok.SneakyThrows;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class JiraCloudConnector {

    private final HttpClient client;
    private final HttpRequest request;

    public JiraCloudConnector() {
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var params = ("?jql=project = \"" + JiraConfig.PROJECT + "\" AND Sprint = \"" + JiraConfig.SPRINT_NAME
                + "\" AND timespent != 0" + "&maxResults=" + JiraConfig.MAX_RESULTS + "&fields=" + JiraConfig.FIELDS)
                .replace(" ", "%20")
                .replace("\"", "%22");

        request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(JiraConfig.SITE_URL + "/rest/api/3/search" + params))
                .header("Authorization", "Basic "
                        + Base64.getEncoder().encodeToString((JiraConfig.USER_EMAIL + ":" + JiraConfig.API_TOKEN).getBytes()))
                .build();

    }


    public CompletableFuture<List<LogWorkEntry>> getAllIssuesAsync() {
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResults)
                .thenApply(this::parseToEntity);
    }

    @SneakyThrows
    private JSONArray parseJsonResults(HttpResponse<String> rb) {
        var results = new JSONObject(rb.body());
        return results.getJSONArray("issues");
    }

    private List<LogWorkEntry> parseToEntity(JSONArray array) {
        var list = new LinkedList<LogWorkEntry>();

        array.forEach(o -> {
            var obj = (JSONObject) o;
            obj.getJSONObject("fields").getJSONObject("worklog").getJSONArray("worklogs").forEach(wl -> {
                var log = (JSONObject) wl;
                try {
                    list.add(LogWorkEntry.builder()
                            .taskId(obj.getString("key"))
                            .userTask(getSummary(obj))
                            .userName(log.getJSONObject("author").getString("displayName"))
                            .logWorkDescription(log.getJSONObject("comment").getJSONArray("content").getJSONObject(0).getJSONArray("content").getJSONObject(0).getString("text"))
                            .logWorkDate(log.getString("created"))
                            .logWorkSeconds(log.getInt("timeSpentSeconds"))
                            .logWorkDateTime(LocalDateTime.parse(log.getString("created").replaceFirst("\\.[0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]", "")))
                            .build());
                }
                catch (Exception e) {
                    System.err.println("Caught \"" + e.getMessage() + "\" on a worklog from " + obj.getString("key"));
                }
            });
        });

        return list;
    }

    private String getSummary(JSONObject obj) {
        StringBuilder sb = new StringBuilder();
        if (obj.getJSONObject("fields").has("parent"))
            sb.append(obj.getJSONObject("fields").getJSONObject("parent").getJSONObject("fields").getString("summary")).append(" ");

        sb.append(obj.getJSONObject("fields").getString("summary"));
        return sb.toString();
    }
}
