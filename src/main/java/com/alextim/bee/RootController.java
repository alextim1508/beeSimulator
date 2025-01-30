package com.alextim.bee;

import com.alextim.bee.context.AppState;
import com.alextim.bee.frontend.MainWindow;
import com.alextim.bee.frontend.view.data.DataController;
import com.alextim.bee.frontend.view.magazine.MagazineController;
import com.alextim.bee.server.messages.DetectorCommands.*;
import com.alextim.bee.server.messages.DetectorMsg;
import com.alextim.bee.server.protocol.DetectorCodes.BDType;
import com.alextim.bee.server.protocol.DetectorCodes.Error;
import com.alextim.bee.server.protocol.DetectorCodes.RestartParam;
import com.alextim.bee.server.protocol.DetectorCodes.RestartReason;
import com.alextim.bee.server.transfer.DetectorClientAbstract;
import com.alextim.bee.service.DetectorService;
import javafx.application.Platform;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.alextim.bee.server.transfer.DetectorParser.parse;

@Slf4j
public class RootController extends RootControllerInitializer {

    public RootController(AppState appState,
                          MainWindow mainWindow,
                          DetectorClientAbstract detectorClient,
                          DetectorService detectorService) {
        super(appState, mainWindow, detectorClient, detectorService);
    }

    protected final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private Future<?> lifeCircleTask;

    @SneakyThrows
    public void listenDetectorClient() {
        Runnable task = () -> {
            MagazineController magazineController = (MagazineController) getChild(MagazineController.class.getSimpleName());

            while (!Thread.currentThread().isInterrupted()) {
                DetectorMsg msg;
                try {
                    msg = detectorClient.waitAndGetDetectorMsg();
                } catch (InterruptedException e) {
                    log.error("Detector client Interrupted Exception");
                    break;
                }

                DetectorMsg detectorMsg = parse(msg);
                log.info("DetectorMsg: {}. {}", detectorMsg.getClass().getSimpleName(), detectorMsg);

                if (detectorMsg instanceof SomeCommand command) {
                    try {
                        handleCommand(command);
                    } catch (Exception e) {
                        log.error("handleCommandAnswer exception", e);
                    }
                }

                magazineController.addLog(detectorMsg);
            }
            log.info("queue handle task is done");
        };
        executorService.submit(task);

        executorService.submit(detectorClient::connect);

        lifeCircleTask = executorService.submit(this::lifeCircle);
    }

    private void handleCommand(SomeCommand detectorMsg) {
        if (detectorMsg instanceof SetSensitivityCommand command) {
            detectorService.sendSetSensitivityAnswer(command.sensitivity);

        } else if (detectorMsg instanceof GetSensitivityCommand command) {
            detectorService.sendGetSensitivityAnswer();

        } else if (detectorMsg instanceof SetDeadTimeCommand command) {
            detectorService.sendSetDeadTimeAnswer(command.deadTime);

        } else if (detectorMsg instanceof GetDeadTimeCommand command) {
            detectorService.sendGetDeadTimeAnswer();

        } else if (detectorMsg instanceof SetCounterCorrectCoeffCommand command) {
            detectorService.sendSetCounterCorrectCoeffAnswer(command.counterCorrectCoeff, (int) command.counterIndex);

        } else if (detectorMsg instanceof GetCounterCorrectCoeffCommand command) {
            detectorService.sendGetCounterCorrectCoeffCommand((int) command.counterIndex);

        } else if (detectorMsg instanceof GetVersionCommand command) {
            detectorService.sendGetVersionAnswer();

        } else if (detectorMsg instanceof SetGeoDataCommand command) {
            detectorService.sendSetGeoDataAnswer(command.lat, command.lon);

        } else if (detectorMsg instanceof RestartDetectorCommand command) {
            detectorService.sendRestartDetectorState();

            lifeCircleTask.cancel(true);
            lifeCircleTask = executorService.submit(this::lifeCircle);
        }
        if (detectorMsg instanceof SetMeasTimeCommand command) {
            detectorService.sendSetMeasTimeAnswer(command.measTime);

            lifeCircleTask.cancel(true);
            lifeCircleTask = executorService.submit(this::measLifeCircle);
        }
    }

    public void sendError(Error error) {
        detectorService.sendError(error);
    }

    public void sendRestart(RestartReason restartReason, RestartParam restartParam) {
        detectorService.sendRestart(restartReason, restartParam);

        lifeCircleTask.cancel(true);
        lifeCircleTask = executorService.submit(this::lifeCircle);
    }

    public void showMeasData(float measData) {
        DataController dataController = (DataController) getChild(DataController.class.getSimpleName());
        Platform.runLater(() -> dataController.showMeasData(
                String.format(Locale.US, "%f", measData)
        ));
    }

    @SneakyThrows
    private void lifeCircle() {
        detectorService.getDetectorStateService().reset();
        Thread.sleep(2000);

        detectorService.sendInitEvent();
        Thread.sleep(1000);

        measLifeCircle();
    }

    @SneakyThrows
    private void measLifeCircle() {
        DataController dataController = (DataController) getChild(DataController.class.getSimpleName());

        for (int i = 0; i < detectorService.getDetectorStateService().getMeasTime(); i++) {
            detectorService.sendAccumulationEvent(
                    i + 1,
                    detectorService.getDetectorStateService().getMeasTime());

            Thread.sleep(1000);
        }

        do {
            int[] counters = dataController.getCounters();

            BDType bdType = dataController.getBDType();

            detectorService.sendMeasEvent(bdType, counters);

            showMeasData(detectorService.getDetectorStateService().getCurMeasData());

            Thread.sleep(1000);
        } while (!Thread.currentThread().isInterrupted());
    }

    public void sendDetectorCommand(DetectorMsg detectorMsg) {
        detectorClient.sendCommand(detectorMsg);

        MagazineController magazineController = (MagazineController) getChild(MagazineController.class.getSimpleName());
        magazineController.addLog(detectorMsg);
    }

    public void close() {
        log.info("close");

        try {
            appState.saveParam();
        } catch (Exception e) {
            log.error("SaveParams error", e);
        }

        try {
            detectorClient.close();
        } catch (Exception e) {
            log.error("detector client shutdown error", e);
        }

        executorService.shutdownNow();
        log.info("scheduledExecutorService shutdown OK");

        try {
            boolean res = executorService.awaitTermination(500, TimeUnit.MILLISECONDS);
            log.info("executorService is terminated: {}", res);
        } catch (InterruptedException e) {
            log.error("executorService.awaitTermination", e);
        }
    }
}

