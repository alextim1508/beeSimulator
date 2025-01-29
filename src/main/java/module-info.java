module com.alextim.bee {
    requires static lombok;
    requires org.slf4j;

    requires javafx.controls;
    requires javafx.fxml;

    opens com.alextim.bee.frontend.view.data to javafx.fxml;
    opens com.alextim.bee.frontend.view.magazine to javafx.fxml;

    exports com.alextim.bee;
    exports com.alextim.bee.service;
}