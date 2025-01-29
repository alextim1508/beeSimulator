package com.alextim.bee.server.transfer;

import com.alextim.bee.server.messages.DetectorMsg;
import com.alextim.bee.server.messages.ExceptionMessage;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

import static com.alextim.bee.server.messages.DetectorCommands.*;
import static com.alextim.bee.server.protocol.DetectorCodes.Command;
import static com.alextim.bee.server.protocol.DetectorCodes.Format.DATA;


@Slf4j
public class DetectorParser {

    public static DetectorMsg parse(DetectorMsg msg) {
        log.debug("========Parse msg========");
        try {
            if (msg.getClass() == SomeCommand.class) {
                return parseCommand((SomeCommand) msg);
            } else {
                return msg;
            }
        } catch (Exception e) {
            log.error("", e);
            return new ExceptionMessage(
                    msg.detectorID,
                    msg.time,
                    new RuntimeException("Ошибка разбора сообщения"),
                    msg.data);
        }
    }


    static DetectorMsg parseCommand(SomeCommand command) {
        log.debug("CommandCode: {}", command.commandCode.title);

        if (command.commandCode.code == Command.RESTART.code) {
            log.debug("RESTART: {}", getHexString(command));

            return new RestartDetectorCommand(command);

        } else if (command.commandCode.code == Command.GET_VERSION.code) {
            log.debug("GET_VERSION: {}", getHexString(command));

            return new GetVersionCommand(command);

        } else if (command.commandCode.code == Command.SET_MEAS_TIME.code) {
            log.debug("SET_MEAS_TIME: {}", getHexString(command));

            long measTIme = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            command.data[DATA.shift + 3],
                            command.data[DATA.shift + 2],
                            command.data[DATA.shift + 1],
                            command.data[DATA.shift]})
                    .getInt());
            log.debug("measTIme: {}", measTIme);

            return new SetMeasTimeCommand(measTIme, command);

        } else if (command.commandCode.code == Command.SET_SENSITIVITY.code) {
            log.debug("SET_SENSITIVITY: {}", getHexString(command));

            float sensitivity = ByteBuffer.wrap(new byte[]{
                            command.data[DATA.shift + 3],
                            command.data[DATA.shift + 2],
                            command.data[DATA.shift + 1],
                            command.data[DATA.shift]})
                    .getFloat();
            log.debug("Sensitivity: {}", sensitivity);

            return new SetSensitivityCommand(sensitivity, command);

        } else if (command.commandCode.code == Command.GET_SENSITIVITY.code) {
            log.debug("GET_SENSITIVITY: {}", getHexString(command));

            return new GetSensitivityCommand(command);

        } else if (command.commandCode.code == Command.SET_DEAD_TIME.code) {
            log.debug("GET_DEAD_TIME: {}", getHexString(command));

            float deadTime = ByteBuffer.wrap(new byte[]{
                            command.data[DATA.shift + 3],
                            command.data[DATA.shift + 2],
                            command.data[DATA.shift + 1],
                            command.data[DATA.shift]})
                    .getFloat();
            log.debug("DeadTime: {}", deadTime);

            return new SetDeadTimeCommand(deadTime, command);

        } else if (command.commandCode.code == Command.GET_DEAD_TIME.code) {
            log.debug("GET_DEAD_TIME: {}", getHexString(command));

            return new GetDeadTimeCommand(command);

        } else if (command.commandCode.code == Command.SET_CORRECT_COFF.code) {
            log.debug("SET_CORRECT_COFF: {}", getHexString(command));

            long counterIndex = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            command.data[DATA.shift + 3],
                            command.data[DATA.shift + 2],
                            command.data[DATA.shift + 1],
                            command.data[DATA.shift]})
                    .getInt());
            log.debug("CounterIndex: {}", counterIndex);

            float counterCorrectCoff = ByteBuffer.wrap(new byte[]{
                            command.data[DATA.shift + 7],
                            command.data[DATA.shift + 6],
                            command.data[DATA.shift + 5],
                            command.data[DATA.shift + 4]})
                    .getFloat();
            log.debug("CounterCorrectCoff: {}", counterCorrectCoff);

            return new SetCounterCorrectCoeffCommand(counterIndex, counterCorrectCoff, command);

        } else if (command.commandCode.code == Command.GET_CORRECT_COFF.code) {
            log.debug("GET_CORRECT_COFF: {}", getHexString(command));

            long counterIndex = Integer.toUnsignedLong(ByteBuffer.wrap(new byte[]{
                            command.data[DATA.shift + 3],
                            command.data[DATA.shift + 2],
                            command.data[DATA.shift + 1],
                            command.data[DATA.shift]})
                    .getInt());
            log.debug("CounterIndex: {}", counterIndex);

            return new GetCounterCorrectCoeffCommand(counterIndex, command);

        } else if (command.commandCode.code == Command.SET_GEO_DATA.code) {
            log.debug("SET_GEO_DATA: {}", getHexString(command));

            float lat = ByteBuffer.wrap(new byte[]{
                            command.data[DATA.shift + 3],
                            command.data[DATA.shift + 2],
                            command.data[DATA.shift + 1],
                            command.data[DATA.shift]})
                    .getFloat();
            log.debug("lat: {}", lat);

            float lon = ByteBuffer.wrap(new byte[]{
                            command.data[DATA.shift + 3],
                            command.data[DATA.shift + 2],
                            command.data[DATA.shift + 1],
                            command.data[DATA.shift]})
                    .getFloat();
            log.debug("lon: {}", lon);

            return new SetGeoDataCommand(lat, lon, command);
        }

        throw new RuntimeException("Unknown detectorCommandAnswerCode: " + command.commandCode.code);
    }

    static String getHexString(DetectorMsg msg) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < msg.data.length; i++)
            s.append(String.format("%x ", msg.data[i]));
        return s.toString();
    }
}
