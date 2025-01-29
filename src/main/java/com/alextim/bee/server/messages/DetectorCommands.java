package com.alextim.bee.server.messages;

import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static com.alextim.bee.context.Property.OTHER_NUMBER_FORMAT;
import static com.alextim.bee.server.protocol.DetectorCodes.Command;
import static com.alextim.bee.server.protocol.DetectorCodes.Command.*;
import static com.alextim.bee.server.protocol.DetectorCodes.CommandStatus;
import static com.alextim.bee.server.protocol.DetectorCodes.CommandStatus.SUCCESS;
import static com.alextim.bee.server.transfer.UpdDetectorTransfer.wrapToPackage;

public class DetectorCommands {

    public static class RestartDetectorCommand extends SomeCommand {

        public RestartDetectorCommand(SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
        }

        @Override
        public String toString() {
            return Command.RESTART.title;
        }
    }


    public static class GetVersionCommand extends SomeCommand {

        public GetVersionCommand(SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
        }

        @Override
        public String toString() {
            return GET_VERSION.title;
        }
    }

    public static class GetVersionAnswer extends SomeCommandAnswer {

        public final String version;

        public GetVersionAnswer(int detectorID, int detectorTime, String version) {
            super(detectorID, detectorTime, GET_VERSION, SUCCESS,
                    wrapToPackage(detectorID, detectorTime, GET_VERSION, SUCCESS, getData(version)));
            this.version = version;
        }

        @SneakyThrows
        private static byte[] getData(String version) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] bytes = ByteBuffer.allocate(2).putShort((short) version.length()).array();
            outputStream.write(new byte[]{bytes[1], bytes[0]});

            outputStream.write(version.getBytes(StandardCharsets.UTF_8));
            return outputStream.toByteArray();
        }

        @Override
        public String toString() {
            return String.format("%s. Версия: %s", GET_VERSION.title, version);
        }
    }


    public static class SetMeasTimeCommand extends SomeCommand {

        public final long measTime;

        public SetMeasTimeCommand(long measTime, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);

            this.measTime = measTime;
        }

        @Override
        public String toString() {
            return String.format("%s. Времени экспозиции: %d", SET_MEAS_TIME.title, measTime);
        }
    }

    public static class SetMeasTimeAnswer extends SomeCommandAnswer {

        public SetMeasTimeAnswer(int detectorID, int detectorTime) {
            super(detectorID, detectorTime, SET_MEAS_TIME, SUCCESS,
                    wrapToPackage(detectorID, detectorTime, SET_MEAS_TIME, SUCCESS, new byte[0]));
        }

        @Override
        public String toString() {
            return SET_MEAS_TIME.title;
        }
    }


    public static class GetDeadTimeCommand extends SomeCommand {

        public GetDeadTimeCommand(SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
        }

        @Override
        public String toString() {
            return GET_DEAD_TIME.title;
        }
    }

    public static class GetDeadTimeAnswer extends SomeCommandAnswer {

        public final float deadTime;

        public GetDeadTimeAnswer(int detectorID, int detectorTime, float deadTime) {
            super(detectorID, detectorTime, GET_SENSITIVITY, SUCCESS,
                    wrapToPackage(detectorID, detectorTime, GET_DEAD_TIME, SUCCESS, getData(deadTime)));

            this.deadTime = deadTime;
        }

        @SneakyThrows
        private static byte[] getData(float deadTime) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] arrayDeadTime = ByteBuffer.allocate(4).putFloat(deadTime).array();
            outputStream.write(new byte[]{
                    arrayDeadTime[3],
                    arrayDeadTime[2],
                    arrayDeadTime[1],
                    arrayDeadTime[0]});

            return outputStream.toByteArray();
        }

        @Override
        public String toString() {
            return String.format("%s. Мертвое время: " + OTHER_NUMBER_FORMAT, GET_DEAD_TIME.title, deadTime);
        }
    }


    public static class SetDeadTimeCommand extends SomeCommand {

        public final float deadTime;

        public SetDeadTimeCommand(float deadTime, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
            this.deadTime = deadTime;
        }


        @Override
        public String toString() {
            return String.format("%s. Мертвое время: " + OTHER_NUMBER_FORMAT, SET_DEAD_TIME.title, deadTime);
        }
    }

    public static class SetDeadTimeAnswer extends SomeCommandAnswer {

        public SetDeadTimeAnswer(int detectorID, int detectorTime) {
            super(detectorID, detectorTime, SET_DEAD_TIME, SUCCESS,
                    wrapToPackage(detectorID, detectorTime, SET_DEAD_TIME, SUCCESS, new byte[0]));
        }


        @Override
        public String toString() {
            return SET_DEAD_TIME.title;
        }
    }


    public static class SetSensitivityCommand extends SomeCommand {

        public final float sensitivity;

        public SetSensitivityCommand(float sensitivity, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
            this.sensitivity = sensitivity;
        }

        @Override
        public String toString() {
            return String.format("%s. Чувствительность: " + OTHER_NUMBER_FORMAT, SET_SENSITIVITY.title, sensitivity);
        }
    }

    public static class SetSensitivityAnswer extends SomeCommandAnswer {

        public SetSensitivityAnswer(int detectorID, int detectorTime) {
            super(detectorID, detectorTime, SET_SENSITIVITY, SUCCESS,
                    wrapToPackage(detectorID, detectorTime, SET_SENSITIVITY, SUCCESS, new byte[0]));
        }

        @Override
        public String toString() {
            return SET_SENSITIVITY.title;
        }
    }


    public static class GetSensitivityCommand extends SomeCommand {

        public GetSensitivityCommand(SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);
        }

        @Override
        public String toString() {
            return GET_SENSITIVITY.title;
        }
    }

    public static class GetSensitivityAnswer extends SomeCommandAnswer {

        public final float sensitivity;

        public GetSensitivityAnswer(int detectorID, int detectorTime, float sensitivity) {
            super(detectorID, detectorTime, GET_SENSITIVITY, SUCCESS,
                    wrapToPackage(detectorID, detectorTime, GET_SENSITIVITY, SUCCESS, getData(sensitivity)));

            this.sensitivity = sensitivity;
        }

        @SneakyThrows
        private static byte[] getData(float sensitivity) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] arrayDeadTime = ByteBuffer.allocate(4).putFloat(sensitivity).array();
            outputStream.write(new byte[]{arrayDeadTime[3], arrayDeadTime[2], arrayDeadTime[1], arrayDeadTime[0]});

            return outputStream.toByteArray();
        }

        @Override
        public String toString() {
            return String.format("%s. Чувствительность: " + OTHER_NUMBER_FORMAT, GET_SENSITIVITY.title, sensitivity);
        }
    }


    public static class SetCounterCorrectCoeffCommand extends SomeCommand {

        public final long counterIndex;

        public final float counterCorrectCoeff;

        public SetCounterCorrectCoeffCommand(long counterIndex, float counterCorrectCoeff, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);

            this.counterIndex = counterIndex;
            this.counterCorrectCoeff = counterCorrectCoeff;
        }


        @Override
        public String toString() {
            return String.format("%s %d, Корректирующий коэффициент: " + OTHER_NUMBER_FORMAT,
                    SET_CORRECT_COFF.title, counterIndex, counterCorrectCoeff);
        }
    }

    public static class SetCounterCorrectCoeffAnswer extends SomeCommandAnswer {

        public SetCounterCorrectCoeffAnswer(int detectorID, int detectorTime) {
            super(detectorID, detectorTime, SET_CORRECT_COFF, SUCCESS,
                    wrapToPackage(detectorID, detectorTime, SET_CORRECT_COFF, SUCCESS, new byte[0]));
        }

        @Override
        public String toString() {
            return SET_CORRECT_COFF.title;
        }
    }


    public static class GetCounterCorrectCoeffCommand extends SomeCommand {

        public final long counterIndex;

        public GetCounterCorrectCoeffCommand(long counterIndex, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);

            this.counterIndex = counterIndex;
        }

        @Override
        public String toString() {
            return String.format("%s %d", GET_CORRECT_COFF.title, counterIndex);
        }
    }

    public static class GetCounterCorrectCoeffAnswer extends SomeCommandAnswer {

        public final long counterIndex;

        public final float counterCorrectCoeff;

        public GetCounterCorrectCoeffAnswer(int detectorID, int detectorTime,
                                            long counterIndex, float counterCorrectCoeff) {
            super(detectorID, detectorTime, GET_CORRECT_COFF, SUCCESS,
                    wrapToPackage(detectorID, detectorTime, GET_CORRECT_COFF, SUCCESS,
                            getData(counterIndex, counterCorrectCoeff)));

            this.counterIndex = counterIndex;
            this.counterCorrectCoeff = counterCorrectCoeff;
        }

        @SneakyThrows
        private static byte[] getData(long counterNumber, float counterValue) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] bytes = ByteBuffer.allocate(4).putInt((int) counterNumber).array();
            outputStream.write(new byte[]{
                    bytes[3],
                    bytes[2],
                    bytes[1],
                    bytes[0]});

            bytes = ByteBuffer.allocate(4).putFloat(counterValue).array();
            outputStream.write(new byte[]{
                    bytes[3],
                    bytes[2],
                    bytes[1],
                    bytes[0]
            });

            return outputStream.toByteArray();
        }

        @Override
        public String toString() {
            return String.format("%s %d, Корректирующий коэффициент: " + OTHER_NUMBER_FORMAT,
                    GET_CORRECT_COFF.title, counterIndex, counterCorrectCoeff);
        }
    }


    public static class SetGeoDataCommand extends SomeCommand {

        public final float lat;
        public final float lon;

        public SetGeoDataCommand(float lat, float lon, SomeCommand command) {
            super(command.detectorID, command.time, command.commandCode, command.data);

            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public String toString() {
            return SET_GEO_DATA.title;
        }
    }

    public static class SetGeoDataAnswer extends SomeCommandAnswer {

        public SetGeoDataAnswer(int detectorID, int detectorTime ) {
            super(detectorID, detectorTime, SET_GEO_DATA, SUCCESS,
                    wrapToPackage(detectorID, detectorTime, SET_GEO_DATA, SUCCESS, new byte[0]));
        }

        @Override
        public String toString() {
            return SET_GEO_DATA.title;
        }
    }


    public static class SomeCommandAnswer extends SomeCommand {
        public final CommandStatus commandStatusCode;

        public SomeCommandAnswer(int detectorID,
                                 long time,
                                 Command commandCode,
                                 CommandStatus commandStatusCode,
                                 byte[] data) {
            super(detectorID, time, commandCode, data);
            this.commandStatusCode = commandStatusCode;
        }
    }

    public static class SomeCommand extends DetectorMsg {
        public final Command commandCode;

        public SomeCommand(int detectorID,
                           long time,
                           Command commandCode,
                           byte[] data) {
            super(detectorID, time, data);
            this.commandCode = commandCode;
        }
    }
}
