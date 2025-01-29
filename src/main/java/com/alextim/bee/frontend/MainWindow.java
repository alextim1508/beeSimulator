package com.alextim.bee.frontend;


import com.alextim.bee.RootController;
import com.alextim.bee.frontend.view.data.DataView;
import com.alextim.bee.frontend.view.magazine.MagazineView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public class MainWindow {
    public static String ROOT_KEY = "ROOT";
    public static String PARENT_KEY = "PARENT";

    private final Stage owner;

    @Getter
    private Image iconImage;

    @SneakyThrows
    private void loadIcons() {
        @Cleanup
        InputStream iconResAsStream = MainWindow.class.getResourceAsStream("icon/icon.png");
        iconImage = new Image(Objects.requireNonNull(iconResAsStream));
    }

    private ResourceBundle getBundle(RootController rootController, MainWindow mainWindow) {
        return new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                if (key.equals(ROOT_KEY))
                    return rootController;
                if (key.equals(PARENT_KEY))
                    return mainWindow;
                return null;
            }

            @Override
            public Enumeration<String> getKeys() {
                return Collections.enumeration(Arrays.asList(ROOT_KEY, PARENT_KEY));
            }
        };
    }

    public AnchorPane createMainWindow(RootController rootController) {
        log.info("Creation main window");

        ResourceBundle bundle = getBundle(rootController, this);

        TabPane tabPane = new TabPane(
                new Tab("Данные", new DataView().getView(bundle)),
                new Tab("Журнал", new MagazineView().getView(bundle))
        );
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        log.info("basePane is built");

        log.info("Creation main window is created");

        AnchorPane pane = new AnchorPane(tabPane);

        AnchorPane.setTopAnchor(tabPane, 0.);
        AnchorPane.setLeftAnchor(tabPane, 0.);
        AnchorPane.setRightAnchor(tabPane, 0.);
        AnchorPane.setBottomAnchor(tabPane, 0.);

        AnchorPane.setTopAnchor(pane, 0.);
        AnchorPane.setLeftAnchor(pane, 0.);
        AnchorPane.setRightAnchor(pane, 0.);
        AnchorPane.setBottomAnchor(pane, 0.);

        loadIcons();

        return pane;
    }
}
