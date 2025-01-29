package com.alextim.bee.server.dto;

import lombok.Builder;

import java.util.Locale;

import static com.alextim.bee.server.protocol.DetectorCodes.BDType;

@Builder
public class Measurement {
    public final int version;
    public final BDType bdType;
    public final long measTime;
    public final long geoTime;
    public final GeoData geoData;
    public final BdData bdData;

    @Override
    public String toString() {
        return String.format(Locale.US,
                "Версия: %d, Тип блока: %s, Время измерение: %d, " + System.lineSeparator() +
                        "%s" + System.lineSeparator() +
                        "%s",
                version, bdType.title, measTime, bdData, geoData);
    }
}


