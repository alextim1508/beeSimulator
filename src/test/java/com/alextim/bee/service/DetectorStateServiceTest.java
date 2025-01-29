package com.alextim.bee.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.alextim.bee.server.protocol.DetectorCodes.BDType.GAMMA;

public class DetectorStateServiceTest {

    @Test
    public void shouldCalcAverageViaLast5values() {
        DetectorStateService service = new DetectorStateService();

        service.setMeasTime(5);

        service.calcTBDData(GAMMA, new int[]{1, 7, 0, 0});
        service.calcTBDData(GAMMA, new int[]{2, 8, 0, 0});
        service.calcTBDData(GAMMA, new int[]{3, 9, 0, 0});
        service.calcTBDData(GAMMA, new int[]{4, 10, 0, 0});
        service.calcTBDData(GAMMA, new int[]{5, 11, 0, 0});
        service.calcTBDData(GAMMA, new int[]{6, 12, 0, 0});

        Assertions.assertArrayEquals(new float[]{
                (2 + 3 + 4 + 5 + 6) / 5.f,
                (8 + 9 + 10 + 11 + 12) / 5.f,
                0,
                0}, service.aveScores);
    }

    @Test
    public void shouldCalcSumViaAllValues() {
        DetectorStateService service = new DetectorStateService();

        service.setMeasTime(5);

        service.calcTBDData(GAMMA, new int[]{1, 7, 0, 0});
        service.calcTBDData(GAMMA, new int[]{2, 8, 0, 0});
        service.calcTBDData(GAMMA, new int[]{3, 9, 0, 0});
        service.calcTBDData(GAMMA, new int[]{4, 10, 0, 0});
        service.calcTBDData(GAMMA, new int[]{5, 11, 0, 0});
        service.calcTBDData(GAMMA, new int[]{6, 12, 0, 0});

        Assertions.assertArrayEquals(new float[]{
                1 + 2 + 3 + 4 + 5 + 6,
                7 + 8 + 9 + 10 + 11 + 12,
                0,
                0}, service.sumFullScores);
    }

    @Test
    public void shouldCalcSumViaValuesAfterSetMeasTime() {
        DetectorStateService service = new DetectorStateService();

        service.setMeasTime(5);

        service.calcTBDData(GAMMA, new int[]{1, 7, 0, 0});
        service.calcTBDData(GAMMA, new int[]{2, 8, 0, 0});
        service.calcTBDData(GAMMA, new int[]{3, 9, 0, 0});

        service.setMeasTime(5);
        service.calcTBDData(GAMMA, new int[]{4, 10, 0, 0});
        service.calcTBDData(GAMMA, new int[]{5, 11, 0, 0});
        service.calcTBDData(GAMMA, new int[]{6, 12, 0, 0});

        Assertions.assertArrayEquals(new float[]{
                4 + 5 + 6,
                10 + 11 + 12,
                0,
                0}, service.sumAccumulatedScores);
    }

    @SneakyThrows
    @Test
    public void calcMeasDataTest() {
        DetectorStateService service = new DetectorStateService();

        service.setMeasTime(5);

        service.calcTBDData(GAMMA, new int[]{1, 1, 0, 0});
        Thread.sleep(1000);
        service.calcTBDData(GAMMA, new int[]{2, 2, 0, 0});
        Thread.sleep(1000);
        service.calcTBDData(GAMMA, new int[]{0, 0, 0, 0});
        Thread.sleep(1000);
        service.calcTBDData(GAMMA, new int[]{1, 1, 0, 0});
        Thread.sleep(1000);
        service.calcTBDData(GAMMA, new int[]{1, 1, 0, 0});
        Thread.sleep(1000);
        service.calcTBDData(GAMMA, new int[]{2, 2, 0, 0});
        Thread.sleep(1000);
        MeasData measData = service.calcTBDData(GAMMA, new int[]{1, 1, 0, 0});

        System.out.println("measData = " + measData);
    }
}
