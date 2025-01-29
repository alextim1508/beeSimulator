package com.alextim.bee.server.transfer;

import com.alextim.bee.server.messages.DetectorCommands.SomeCommand;
import com.alextim.bee.server.messages.DetectorMsg;
import com.alextim.bee.server.messages.ExceptionMessage;
import com.alextim.bee.server.protocol.DetectorCodes.Command;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

import static com.alextim.bee.server.protocol.DetectorCodes.Format.*;
import static com.alextim.bee.server.protocol.DetectorCodes.MsgType.COMMAND_TYPE;

@Slf4j
public class DetectorClient extends DetectorClientAbstract {

    private final String IP;
    private final int rcvPort;
    private final int trPort;
    private final int rcvBufSize;
    private UpdDetectorTransfer transfer;

    public DetectorClient(String ip,
                          int rcvPrt,
                          int trPort,
                          int rcvBufSize,
                          LinkedBlockingQueue<DetectorMsg> queue ) {
        super(queue);
        this.IP = ip;
        this.rcvPort = rcvPrt;
        this.trPort = trPort;
        this.rcvBufSize = rcvBufSize;

        createTransfer();
    }

    public void createTransfer() {
        transfer = new UpdDetectorTransfer((bytes) -> {
            log.info("========== New detector message ========== ");

            int detectorID = ByteBuffer.wrap(new byte[]{
                            bytes[ID.shift + 3],
                            bytes[ID.shift + 2],
                            bytes[ID.shift + 1],
                            bytes[ID.shift]})
                    .getInt();
            log.info("DetectorID: {}", detectorID);

            long time = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            bytes[TIME.shift + 3],
                            bytes[TIME.shift + 2],
                            bytes[TIME.shift + 1],
                            bytes[TIME.shift]})
                    .getInt());
            log.info("Time: {}", time);

            try {
                if (bytes[TYPE.shift] == COMMAND_TYPE.code) {
                    Command commandByCode = Command.getCommandByCode(bytes[EVT_ANS_CMD.shift]);
                    log.info("Command: {}", commandByCode.title);

                    queue.add(new SomeCommand(detectorID, time, commandByCode, bytes));
                } else {
                    throw new RuntimeException("Unsupported type: " + bytes[TYPE.shift]);
                }
            } catch (Exception e) {
                queue.add(new ExceptionMessage(detectorID, time, e, bytes));
            }

        }, rcvBufSize);
    }

    @Override
    public void connect() {
        transfer.open(IP, rcvPort, trPort);
    }

    @Override
    public void sendCommand(DetectorMsg detectorMsg) {
        transfer.sendData(detectorMsg);
    }

    @Override
    public void close() {
        transfer.close();
    }
}