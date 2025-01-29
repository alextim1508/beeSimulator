package com.alextim.bee;

import com.alextim.bee.context.Context;
import com.alextim.bee.frontend.MainWindow;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import static com.alextim.bee.context.Property.TITLE_APP;

@Slf4j
public class Main extends Application {
    private static String[] args;

    public static void main(String[] args) {
        Main.args = args;
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        log.info("start");
        System.setProperty("file.encoding", "UTF-8");

        MainWindow mainWindow = new MainWindow(stage);

        StartController startController = showStartWindow(stage);

        Thread thread = new Thread(() -> {
            RootController rootController = null;
            try {
                startController.addLog("Создание контекста");
                rootController = new Context(mainWindow, args).getRootController();
            } catch (Exception e) {
                startController.setHeader("Ошибка инициализации");
                startController.addLog(e.getMessage());
            }

            if (rootController == null)
                return;

            RootController finalRootController = rootController;
            Platform.runLater(() -> {
                startController.addLog("Создание графического окна");
                AnchorPane mainWindowPane = mainWindow.createMainWindow(finalRootController);
                startController.addLog("OK");

                initStage(stage,
                        mainWindowPane,
                        mainWindow.getIconImage(),
                        finalRootController);
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    private static StartController showStartWindow(Stage stage) {
        StartController startController = new StartController();
        stage.setScene(new Scene(startController.getStartPane("Инициализация")));
        stage.show();
        return startController;
    }

    private void initStage(Stage stage,
                           AnchorPane rootPane,
                           Image icon,
                           RootController rootController) {
        stage.hide();
        stage.setMaximized(true);
        stage.setScene(new Scene(rootPane));
        stage.setTitle(TITLE_APP);
        stage.getIcons().add(icon);

        stage.setOnShowing(event -> {
            log.info("showing callback");
            rootController.listenDetectorClient();
        });

        stage.setOnCloseRequest(handler -> {
            log.info("shutdown callback");

            rootController.close();
            log.info("Root controller is closed");

            Platform.exit();
            log.info("Platform.exit");
        });

        stage.show();
    }
}