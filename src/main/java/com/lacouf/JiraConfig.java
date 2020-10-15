package com.lacouf;

public class JiraConfig {

    public static final String USER_EMAIL = "TON_EMAIL_ICI";
    public static final String API_TOKEN = "TA_CLE_D_API_ICI"; //https://id.atlassian.com/manage-profile/security pour générer
    public static final String SITE_URL = "https://420-565-eq1.atlassian.net";
    public static final String PROJECT = "EQ3";
    public static final String SPRINT_NAME = "Sprint 3"; //si null, recherche par date seulement
    public static final String START_DATE = "01-01-1900";
    public static final boolean INCLUDE_EMPTY_COMMENT = true;

    //Do not change
    public static final String FIELDS = "id,worklog,parent,summary";
    public static final int MAX_RESULTS = 500;
}
