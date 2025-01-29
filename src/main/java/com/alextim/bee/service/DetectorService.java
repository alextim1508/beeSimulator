package com.alextim.bee.service;

import com.alextim.bee.RootController;
import com.alextim.bee.server.dto.*;
import com.alextim.bee.server.messages.DetectorEvents.*;
import com.alextim.bee.server.protocol.DetectorCodes;
import com.alextim.bee.server.protocol.DetectorCodes.RestartParam;
import com.alextim.bee.server.protocol.DetectorCodes.RestartReason;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import static com.alextim.bee.context.Property.*;
import static com.alextim.bee.server.messages.DetectorCommands.*;
import static com.alextim.bee.server.protocol.DetectorCodes.BDInternalMode.BD_MODE_PULSE;
import static com.alextim.bee.server.protocol.DetectorCodes.RestartReason.RESTART_COMMAND;


@RequiredArgsConstructor
public class DetectorService {

    @Getter
    private final DetectorStateService detectorStateService;

    @Setter
    private RootController rootController;

    public void sendSetMeasTimeAnswer(long measTime) {
        detectorStateService.setMeasTime((int) measTime);

        rootController.sendDetectorCommand(new SetMeasTimeAnswer(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime()));
    }

    public void sendSetSensitivityAnswer(float sensitivity) {
        detectorStateService.setSensitivity(sensitivity);

        rootController.sendDetectorCommand(new SetSensitivityAnswer(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime()));
    }

    public void sendGetSensitivityAnswer() {
        rootController.sendDetectorCommand(new GetSensitivityAnswer(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime(),
                detectorStateService.getSensitivity()));
    }

    public void sendSetDeadTimeAnswer(float deadTime) {
        detectorStateService.setDeadTime(deadTime);

        rootController.sendDetectorCommand(new SetDeadTimeAnswer(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime()));
    }

    public void sendGetDeadTimeAnswer() {
        rootController.sendDetectorCommand(new GetDeadTimeAnswer(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime(),
                detectorStateService.getDeadTime()));
    }

    public void sendSetCounterCorrectCoeffAnswer(float counterCorrectCoeff, int counterIndex) {
        detectorStateService.setCounterCorrect(counterCorrectCoeff, counterIndex);

        rootController.sendDetectorCommand(new SetCounterCorrectCoeffAnswer(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime()));
    }

    public void sendGetCounterCorrectCoeffCommand(int counterIndex) {
        rootController.sendDetectorCommand(new GetCounterCorrectCoeffAnswer(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime(),
                counterIndex,
                detectorStateService.getCounterCorrect(counterIndex)));
    }

    public void sendGetVersionAnswer() {
        rootController.sendDetectorCommand(new GetVersionAnswer(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime(),
                detectorStateService.getVersion()));
    }

    public void sendSetGeoDataAnswer(float lat, float lon) {
        detectorStateService.setGeoData(lat, lon);

        rootController.sendDetectorCommand(new SetGeoDataAnswer(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime()));
    }

    public void sendRestartDetectorState() {
        rootController.sendDetectorCommand(new RestartDetectorState(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime(),
                RESTART_COMMAND,
                null,
                DETECTOR_IP_ADDR,
                SOURCE_IP_ADDR,
                TRANSFER_RCV_PORT,
                TRANSFER_TR_PORT
        ));
    }

    public void sendError(DetectorCodes.Error error) {
        rootController.sendDetectorCommand(new ErrorDetectorState(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime(),
                error)
        );
    }

    public void sendRestart(RestartReason restartReason, RestartParam restartParam) {
        rootController.sendDetectorCommand(new RestartDetectorState(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime(),
                restartReason,
                restartParam,
                DETECTOR_IP_ADDR,
                SOURCE_IP_ADDR,
                TRANSFER_RCV_PORT,
                TRANSFER_TR_PORT)
        );
    }

    public void sendInitEvent() {
        rootController.sendDetectorCommand(new InitializationDetectorState(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime()));
    }

    public void sendAccumulationEvent(int curTime, int measTime) {
        rootController.sendDetectorCommand(new AccumulationDetectorState(
                        TRANSFER_TO_DETECTOR_ID,
                        detectorStateService.getDetectorTime(),
                        curTime,
                        measTime));
    }

    public void sendMeasEvent(DetectorCodes.BDType bdType, int[] counters) {
        MeasData measData = detectorStateService.calcTBDData(bdType, counters);

        InternalData internalData;
        if (bdType == DetectorCodes.BDType.GAMMA) {
            internalData = BdmgInternalData.builder()
                    .voltage400V(400)
                    .version(1)
                    .bdType(bdType)
                    .measTime(detectorStateService.getMeasTime())
                    .mode(BD_MODE_PULSE)
                    .reserve(0)
                    .currentScores(detectorStateService.getCurScores())
                    .averageScores(detectorStateService.getAveScores())
                    .temperature(12.1f)
                    .build();
        } else {
            internalData = BdpnInternalData.builder()
                    .voltage500V(500)
                    .voltage2500V(2500)
                    .version(1)
                    .bdType(bdType)
                    .measTime(detectorStateService.getMeasTime())
                    .mode(BD_MODE_PULSE)
                    .reserve(0)
                    .currentScores(detectorStateService.getCurScores())
                    .averageScores(detectorStateService.getAveScores())
                    .temperature(12.1f)
                    .build();
        }

        InternalEvent internalEvent = new InternalEvent(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime(),
                internalData);

        BdData bdData;
        if (bdType == DetectorCodes.BDType.GAMMA) {
            bdData = BdmgData.builder()
                    .currentScore(measData.curScore)
                    .averageScore(measData.aveScore)
                    .currentMED(measData.curMeasData)
                    .averageMED(measData.aveMeasData)
                    .accumulatedMED(measData.accDoseT)
                    .accumulatedPowerMEDP(measData.accDoseP)
                    .accumulatedTime(measData.accInterval)
                    .build();
        } else {
            bdData = BdpnData.builder()
                    .currentScore(measData.curScore)
                    .averageScore(measData.aveScore)
                    .currentDensity(measData.curMeasData)
                    .averageDensity(measData.aveMeasData)
                    .accumulatedScore(measData.accDoseT)
                    .accumulatedPowerScore(measData.accDoseP)
                    .accumulatedTime(measData.accInterval)
                    .build();
        }

        Measurement measurement = Measurement.builder()
                .version(1)
                .bdType(bdType)
                .measTime(detectorStateService.getMeasTime())
                .geoTime(detectorStateService.getGeoTime())
                .geoData(new GeoData(detectorStateService.getLat(), detectorStateService.getLon()))
                .bdData(bdData)
                .build();

        MeasurementDetectorState measDetectorState = new MeasurementDetectorState(
                TRANSFER_TO_DETECTOR_ID,
                detectorStateService.getDetectorTime(),
                measurement);

        rootController.sendDetectorCommand(measDetectorState);

        rootController.sendDetectorCommand(internalEvent);
    }
}