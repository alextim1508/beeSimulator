package com.alextim.bee.server.transfer;

import com.alextim.bee.server.messages.DetectorMsg;
import lombok.AllArgsConstructor;

import java.util.concurrent.LinkedBlockingQueue;

@AllArgsConstructor
public abstract class DetectorClientAbstract {

    protected final LinkedBlockingQueue<DetectorMsg> queue;

    public abstract void connect();
    public abstract void sendCommand(DetectorMsg detectorMsg);
    public abstract void close();

    public DetectorMsg waitAndGetDetectorMsg() throws InterruptedException {
        return queue.take();
    }

    public void clear() {
        queue.clear();
    }
}
