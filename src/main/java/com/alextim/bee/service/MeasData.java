package com.alextim.bee.service;

import java.util.Locale;


public class MeasData {
    public float curScore;
    public float aveScore;
    public float curMeasData;
    public float aveMeasData;
    public int accInterval;
    public float accDoseT;
    public float accDoseP;

    @Override
    public String toString() {
        return String.format(Locale.US, "MeasData { " +
                "curScore: %f, aveScore: %f, curMeasData: %f, aveMeasData: %f, " +
                "accInterval: %d, accDoseT: %f, accDoseP: %f }",
                curScore, aveScore, curMeasData, aveMeasData, accInterval, accDoseT , accDoseP);
    }
}
