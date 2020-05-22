package com.borkozic.data;

public class Situation {
    public double speed;
    public double track;
    public double altitude; //= Integer.MIN_VALUE;
    public long time;
    public String name;
    public long id;
    public double latitude;
    public double longitude;
    public boolean silent;

    public Situation() {
        speed = 0;
        track = 0;
        time = 0;
        altitude=0;
    }

    public Situation(String name) {
        this();
        this.name = name;
    }
}
