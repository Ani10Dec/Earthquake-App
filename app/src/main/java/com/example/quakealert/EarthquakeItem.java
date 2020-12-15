package com.example.quakealert;

public class EarthquakeItem {
    private final String mag;
    private final String location1;
    private final String location2;
    private final String time;
    private final String url;

    public EarthquakeItem(String mag, String location1, String location2, String time, String url) {
        this.mag = mag;
        this.location1 = location1;
        this.location2 = location2;
        this.time = time;
        this.url = url;
    }

    public String getMag() {
        return mag;
    }

    public String getLocation1() {
        return location1;
    }

    public String getLocation2() {
        return location2;
    }

    public String getTime() {
        return time;
    }

    public String getUrl() {
        return url;
    }
}
