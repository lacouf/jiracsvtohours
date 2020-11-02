package com.lacouf;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;

public class JiraCloudConnector {

    public static final boolean INCLUDE_EMPTY_COMMENT = true;
    public static final String FIELDS = "id,parent,summary";

    private final HttpClient client;
    private final HttpRequest issuesRequest;
    private final Map<String, String> props;
    private final Map<Integer, JSONObject> issues;

    public JiraCloudConnector(Map<String, String> props) {
        this.props = props;
        issues = new HashMap<>();
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var params = ("?jql=project=" + props.get("project") +
                (props.get("sprint") != null ? " AND sprint = \"" + props.get("sprint") + "\"" : "") +
                " AND timespent != 0" + "&fields=" + FIELDS)
                .replace(" ", "%20")
                .replace("\"", "%22");

        issuesRequest = HttpRequest.newBuilder()
                .GET()
                .uri(getIssuesUrl(params))
                .header("Authorization", "Basic "
                        + Base64.getEncoder().encodeToString((props.get("email") + ":" + props.get("apitoken")).getBytes()))
                .build();

    }

    private URI getIssuesUrl(String params) {
        final URI url = URI.create(props.get("url") + "/rest/api/3/search" + params);
        System.out.println(url);
        return url;
    }

    private URI getWorklogsUrl(int issueId) {
        return URI.create(props.get("url") + "/rest/api/3/issue/" + issueId + "/worklog");
    }

    public List<LogWorkEntry> getAllWorklogs() {
        var logs = new LinkedList<LogWorkEntry>();

        client.sendAsync(issuesRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(this::parseJsonResults)
                .join()
                .forEach(i -> {
                    var issue = (JSONObject) i;
                    issues.put(issue.getInt("id"), issue);
                });

        issues.keySet().forEach(issueId -> {
            final var req = HttpRequest.newBuilder()
                    .GET()
                    .uri(getWorklogsUrl(issueId))
                    .header("Authorization", "Basic "
                            + Base64.getEncoder().encodeToString((props.get("email") + ":" + props.get("apitoken")).getBytes()))
                    .build();

            logs.addAll(client.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                    .thenApply(this::parseToEntity)
                    .join());
        });

        return logs;
    }

    private JSONArray parseJsonResults(HttpResponse<String> rb) {
        try {
            var results = new JSONObject(rb.body());
            return results.getJSONArray("issues");
        }
        catch (Exception e) {
            System.err.println("---- Erreur de parsing de la réponse ----");
            System.err.println(e.getLocalizedMessage());
            System.err.println("Réponse de JIRA: ");
            System.err.println(rb.body());
            System.exit(1);
        }
        return null;
    }

    private List<LogWorkEntry> parseToEntity(HttpResponse<String> rb) {
        var list = new LinkedList<LogWorkEntry>();
        var array = new JSONObject(rb.body()).getJSONArray("worklogs");

        array.forEach(o -> {
                var log = (JSONObject) o;
                try {
                    list.add(LogWorkEntry.builder()
                            .taskId(issues.get(log.getInt("issueId")).getString("key"))
                            .userTask(getSummary(issues.get(log.getInt("issueId"))))
                            .userName(log.getJSONObject("author").getString("displayName"))
                            .logWorkDescription(getComment(log))
                            .logWorkDate(log.getString("created"))
                            .logWorkSeconds(log.getInt("timeSpentSeconds"))
                            .logWorkDateTime(LocalDateTime.parse(log.getString("created").replaceFirst(
                                    "\\.[0-9][0-9][0-9]-[0-9][0-9][0-9][0-9]",
                                    "")))
                            .build());
                }
                catch (Exception e) {
                    System.err.println("Caught \"" + e.getMessage() + "\" on a worklog from " + issues.get(log.getInt("issueId")).getString("key") + " by " + log.getJSONObject(
                            "author").getString("displayName"));
                }
            });

        return list;
    }

    private String getComment(JSONObject log) {
        String value;
        try {
            value = log.getJSONObject("comment").getJSONArray("content").getJSONObject(0).getJSONArray("content").getJSONObject(
                    0).getString("text");
        }
        catch (Exception e) {
            if (INCLUDE_EMPTY_COMMENT)
                value = "--- EMPTY COMMENT ---";
            else
                throw e;
        }
        return value;
    }

    private String getSummary(JSONObject obj) {
        StringBuilder sb = new StringBuilder();
        if (obj.getJSONObject("fields").has("parent"))
            sb.append(obj.getJSONObject("fields").getJSONObject("parent").getJSONObject("fields").getString("summary")).append(
                    " ");

        sb.append(obj.getJSONObject("fields").getString("summary"));
        return sb.toString();
    }
}
