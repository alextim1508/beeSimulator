package com.alextim.bee.frontend.view.data;

import com.alextim.bee.frontend.view.NodeController;
import com.alextim.bee.server.protocol.DetectorCodes.Error;
import com.alextim.bee.server.protocol.DetectorCodes.RestartParam;
import com.alextim.bee.server.protocol.DetectorCodes.RestartReason;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ResourceBundle;

import static com.alextim.bee.server.protocol.DetectorCodes.BDType;
import static com.alextim.bee.server.protocol.DetectorCodes.RestartReason.RESTART_ERROR;
import static com.alextim.bee.server.protocol.DetectorCodes.RestartReason.RESTART_POWER;


@Slf4j
public class DataController extends NodeController {

    private final String BDMG_MEAS_DATA_TITLE = "МАЭД (мЗв/час): ";
    private final String BDPN_MEAS_DATA_TITLE = "ППН (нейтр/см² сек): ";

    @FXML
    ComboBox<Error> codeErr;

    @FXML
    ComboBox<RestartReason> restartReason;
    @FXML
    ComboBox<RestartParam> restartParam;

    @FXML
    ToggleGroup bdTypeGroup;
    @FXML
    RadioButton bdTypeMG, bdTypePN;
    @FXML
    Label measDataLabel;
    @FXML
    TextField measDataField;

    @FXML
    Slider curCounter1, curCounter2, curCounter3, curCounter4;

    @Override
    protected String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        initDbTypeGroup();
        initErrorComboBox();
        initRestartComboBox();
    }

    private void initDbTypeGroup() {
        bdTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == bdTypeMG) {
                measDataLabel.setText(BDMG_MEAS_DATA_TITLE);
                curCounter3.setDisable(true);
                curCounter4.setDisable(true);
            } else {
                measDataLabel.setText(BDPN_MEAS_DATA_TITLE);
                curCounter3.setDisable(false);
                curCounter4.setDisable(false);
            }
        });
    }

    private void initErrorComboBox() {
        codeErr.setItems(FXCollections.observableArrayList(Error.values()));
        codeErr.getSelectionModel().selectFirst();
    }

    private void initRestartComboBox() {
        restartReason.setItems(FXCollections.observableArrayList(RESTART_POWER, RESTART_ERROR));
        restartReason.getSelectionModel().selectFirst();

        restartReason.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue == RESTART_ERROR) {
                restartParam.setDisable(false);
                restartParam.setItems(FXCollections.observableArrayList(RestartParam.values()));
                restartParam.getSelectionModel().selectFirst();
            } else {
                restartParam.setDisable(true);
                restartParam.setItems(null);
            }
        });
    }

    @FXML
    void sendErrorOn(ActionEvent event) {
        rootController.sendError(codeErr.getSelectionModel().getSelectedItem());
    }

    @FXML
    void sendRestartOn(ActionEvent event) {
        rootController.sendRestart(
                restartReason.getSelectionModel().getSelectedItem(),
                restartParam.getSelectionModel().getSelectedItem());
    }

    public int[] getCounters() {
        return new int[]{
                (int) curCounter1.getValue(),
                (int) curCounter2.getValue(),
                (int) curCounter3.getValue(),
                (int) curCounter4.getValue()
        };
    }

    public BDType getBDType() {
        return bdTypeGroup.getSelectedToggle() == bdTypeMG ?
                BDType.GAMMA :
                BDType.NEUTRON;
    }

    public void showMeasData(String text) {
        measDataField.setText(text);
    }
}
