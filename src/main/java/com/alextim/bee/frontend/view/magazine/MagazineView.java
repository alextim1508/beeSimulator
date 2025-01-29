package com.alextim.bee.frontend.view.magazine;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ResourceBundle;

@Slf4j
public class MagazineView {
    public Pane getView(ResourceBundle bundle) {
        try {
            return FXMLLoader.<AnchorPane>load(MagazineView.class.getResource("MagazineView.fxml"), bundle);
        } catch (IOException e) {
            log.error("", e);

            return new Pane(new Label("error"));
        }
    }
}
