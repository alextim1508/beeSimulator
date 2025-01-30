package com.alextim.bee.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

public class DetectorStateServiceInitializer {

    long startTime;

    @Getter
    int measTime = 10;

    @Setter
    @Getter
    float deadTime = 0.000005f;

    @Getter
    @Setter
    float sensitivity = 1f;

    final float[] counterCorrect = new float[] {
            1f, 1f, 1f, 1f
    };

    @Getter
    final String version = "version";

    @Getter
    int geoTime;
    @Getter
    float lat, lon;

    public int getDetectorTime() {
        return (int) (System.currentTimeMillis() - startTime);
    }

    public void setGeoData(float lat, float lon) {
        geoTime = (int) (System.currentTimeMillis() - startTime);
        this.lat = lat;
        this.lon = lon;
    }

    public void setCounterCorrect(float value, int index) {
        counterCorrect[index - 1] = value;
    }

    public float getCounterCorrect(int index) {
        return counterCorrect[index - 1];
    }
}
