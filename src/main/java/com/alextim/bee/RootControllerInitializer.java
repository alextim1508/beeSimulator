package com.alextim.bee;

import com.alextim.bee.context.AppState;
import com.alextim.bee.frontend.MainWindow;
import com.alextim.bee.frontend.view.NodeController;
import com.alextim.bee.server.transfer.DetectorClientAbstract;
import com.alextim.bee.service.DetectorService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class RootControllerInitializer {

    protected final AppState appState;

    protected final MainWindow mainWindow;

    protected final DetectorClientAbstract detectorClient;

    protected final DetectorService detectorService;

    protected final Map<String, NodeController> children = new HashMap<>();

    public void addChild(String name, NodeController child) {
        children.put(name, child);
    }

    public NodeController getChild(String name) {
        return children.get(name);
    }
}
