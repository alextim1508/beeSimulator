package com.alextim.bee.service;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.alextim.bee.server.protocol.DetectorCodes.BDType;

@Slf4j
public class DetectorStateService extends DetectorStateServiceInitializer {

    private final int NEUTRON_COUNTERS_AMOUNT = 4;
    private final int GAMMA_COUNTERS_AMOUNT = 2;

    private int COUNTERS_AMOUNT = NEUTRON_COUNTERS_AMOUNT;

    private long accumulationStartTime;

    private final List<LinkedList<Integer>> queues = Arrays.asList(
            new LinkedList<>(),
            new LinkedList<>(),
            new LinkedList<>(),
            new LinkedList<>());

    @Getter
    float[] curScores = new float[COUNTERS_AMOUNT];
    float[] sumFullScores = new float[COUNTERS_AMOUNT];
    float[] sumAccumulatedScores = new float[COUNTERS_AMOUNT];
    @Getter
    float[] aveScores = new float[COUNTERS_AMOUNT];

    @Getter
    private float curMeasData;

    public DetectorStateService() {
        super(System.currentTimeMillis());
        accumulationStartTime = startTime;
    }

    public void setMeasTime(int measTime) {
        this.measTime = measTime;

        queues.forEach(LinkedList::clear);

        accumulationStartTime = System.currentTimeMillis();

        Arrays.fill(sumAccumulatedScores, 0);
    }


    private float calsMeasSS(float[] scores, int timeStep) {
        if (timeStep == 0)
            return 0;

        float SS = 0;
        for (int i = 0; i < COUNTERS_AMOUNT; i++)
            SS += ((scores[i] * counterCorrect[i]) / (1 - scores[i] * deadTime / timeStep));
        return (SS / COUNTERS_AMOUNT);
    }

    private MeasData calcGammaMeasData(float curScore, float aveScore,
                                       float curSS, float aveSS, float accSS, float fulSS, int accInterval) {
        MeasData pBDData = new MeasData();

        float countersSensitivity = 7.5f;  // чувствительность счетчиков, имп/мкР
        float ren2ziv = 0.01053f;  // перевод рентген -> зиверт

        float coff = ren2ziv / countersSensitivity / 1000000.0f;

        pBDData.curScore = curScore;
        pBDData.aveScore = aveScore;
        pBDData.aveMeasData = 1000 * coff * aveSS;
        pBDData.curMeasData = 1000 * coff * curSS;
        pBDData.accDoseT = coff * accSS;
        pBDData.accDoseP = coff * fulSS;
        pBDData.accInterval = accInterval;

        return pBDData;
    }

    private MeasData calcNeutronMeasData(float curScore, float aveScore,
                                         float curSS, float aveSS, float accSS, float fulSS, int accInterval) {
        MeasData pBDData = new MeasData();

        float countersSquare = 1.0f;  // площадь чувствительной части счетчика, см2

        pBDData.curScore = curScore;
        pBDData.aveScore = aveScore;
        pBDData.curMeasData = curSS / countersSquare;
        pBDData.aveMeasData = aveSS / countersSquare;
        pBDData.accDoseT = accSS;
        pBDData.accDoseP = fulSS;
        pBDData.accInterval = accInterval;

        return pBDData;
    }

    public MeasData calcTBDData(BDType bdType, int[] counters) {
        if (bdType == BDType.GAMMA) {
            COUNTERS_AMOUNT = GAMMA_COUNTERS_AMOUNT;
        } else {
            COUNTERS_AMOUNT = NEUTRON_COUNTERS_AMOUNT;
        }

        float curScore = 0.0f;
        for (int i = 0; i < COUNTERS_AMOUNT; i++) {
            curScores[i] = counters[i];
            curScore += counters[i];
        }
        log.info("curScore: {}", curScore);
        log.info("curScores: {}", curScores);

        for (int i = 0; i < COUNTERS_AMOUNT; i++) {
            sumFullScores[i] += counters[i];
            sumAccumulatedScores[i] += counters[i];
        }
        log.info("sumFullScores: {}", sumFullScores);
        log.info("sumAccumulatedScores: {}", sumAccumulatedScores);

        for (int i = 0; i < COUNTERS_AMOUNT; i++) {
            LinkedList<Integer> queue = queues.get(i);
            queue.add(counters[i]);

            if (queue.size() > measTime) {
                queue.removeFirst();
            }
        }

        float aveScore = 0.0f;
        for (int i = 0; i < COUNTERS_AMOUNT; i++) {
            LinkedList<Integer> queue = queues.get(i);

            int sum = 0;
            for (int num : queue) {
                sum += num;
            }

            aveScores[i] = 1.f * sum / queue.size();
            aveScore += aveScores[i];
        }
        log.info("aveScore: {}", aveScore);
        log.info("aveScores: {}", aveScores);

        float curSS = sensitivity * calsMeasSS(curScores, 1);
        log.info("curSS: {}", curSS);
        float aveSS = sensitivity * calsMeasSS(aveScores, 1);
        log.info("aveSS: {}", aveSS);

        int accInterval = (int) ((System.currentTimeMillis() - accumulationStartTime) / 1000);
        log.info("accInterval: {}", accInterval);
        int fulInterval = (int) ((System.currentTimeMillis() - startTime) / 1000);
        log.info("fulInterval: {}", fulInterval);

        float accSS = sensitivity * calsMeasSS(sumAccumulatedScores, accInterval);
        log.info("accSS: {}", accSS);
        float fulSS = sensitivity * calsMeasSS(sumFullScores, fulInterval);
        log.info("fulSS: {}", fulSS);

        MeasData measData;
        if (bdType == BDType.GAMMA) {
             measData = calcGammaMeasData(curScore, aveScore, curSS, aveSS, accSS, fulSS, accInterval);
        } else {
            measData = calcNeutronMeasData(curScore, aveScore, curSS, aveSS, accSS, fulSS, accInterval);
        }
        log.info("measData: {}", measData);

        this.curMeasData = measData.curMeasData;
        return measData;
    }
}
