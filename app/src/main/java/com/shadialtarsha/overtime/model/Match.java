package com.shadialtarsha.overtime.model;

public class Match {

    private String mHomeTeam;
    private String mMatchDate;
    private String mAwayTeam;

    public String getHomeTeam() {
        return mHomeTeam;
    }

    public void setHomeTeam(String homeTeam) {
        mHomeTeam = homeTeam;
    }

    public String getMatchDate() {
        return mMatchDate;
    }

    public void setMatchDate(String matchDate) {
        String[] str_parse = matchDate.split("T");
        String time = str_parse[1];
        String[] time_components = time.split(":");
        mMatchDate = time_components[0] + ":" + time_components[1];
    }

    public String getAwayTeam() {
        return mAwayTeam;
    }

    public void setAwayTeam(String awayTeam) {
        mAwayTeam = awayTeam;
    }
}
