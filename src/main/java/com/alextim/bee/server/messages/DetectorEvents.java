package com.alextim.bee.server.messages;

import com.alextim.bee.server.dto.BdmgInternalData;
import com.alextim.bee.server.dto.BdpnInternalData;
import com.alextim.bee.server.dto.InternalData;
import com.alextim.bee.server.dto.Measurement;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.alextim.bee.server.protocol.DetectorCodes.BDInternalMode.BD_MODE_CONTINUOUS_HIGH_SENS;
import static com.alextim.bee.server.protocol.DetectorCodes.Error;
import static com.alextim.bee.server.protocol.DetectorCodes.*;
import static com.alextim.bee.server.protocol.DetectorCodes.Event.*;
import static com.alextim.bee.server.protocol.DetectorCodes.RestartReason.RESTART_COMMAND;
import static com.alextim.bee.server.protocol.DetectorCodes.RestartReason.RESTART_ERROR;
import static com.alextim.bee.server.protocol.DetectorCodes.State.*;
import static com.alextim.bee.server.transfer.UpdDetectorTransfer.wrapToPackage;

public class DetectorEvents {

    public static class RestartDetectorState extends SomeEvent {

        public final RestartReason reason;
        public final RestartParam param;
        public final int[] detectorIpAddr;
        public final int[] sourceIpAddr;
        public final int ipPort;
        public final int externalDeviceIpPort;

        public RestartDetectorState(int detectorID,
                                    int detectorTime,
                                    RestartReason reason,
                                    RestartParam param,
                                    int[] detectorIpAddr,
                                    int[] sourceIpAddr,
                                    int ipPort,
                                    int externalDeviceIpPort) {
            super(detectorID, detectorTime, RESTART,
                    wrapToPackage(detectorID, detectorTime, RESTART, getData(reason, param, detectorIpAddr, sourceIpAddr, ipPort, externalDeviceIpPort)));
            this.reason = reason;
            this.param = param;
            this.detectorIpAddr = detectorIpAddr;
            this.sourceIpAddr = sourceIpAddr;
            this.ipPort = ipPort;
            this.externalDeviceIpPort = externalDeviceIpPort;
        }

        @SneakyThrows
        private static byte[] getData(RestartReason reason, RestartParam param,
                                      int[] detectorIpAddr, int[] sourceIpAddr,
                                      int ipPort, int ipPortExternal) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            byte[] bytes = ByteBuffer.allocate(4).putInt(reason.code).array();
            outputStream.write(new byte[]{
                    bytes[3],
                    bytes[2],
                    bytes[1],
                    bytes[0]
            });

            if (reason == RESTART_COMMAND) {
                outputStream.write(new byte[]{
                        (byte) sourceIpAddr[0],
                        (byte) sourceIpAddr[1],
                        (byte) sourceIpAddr[2],
                        (byte) sourceIpAddr[3]
                });

            } else if (reason == RESTART_ERROR) {
                bytes = ByteBuffer.allocate(4).putInt(param.code).array();
                outputStream.write(new byte[]{
                        bytes[3],
                        bytes[2],
                        bytes[1],
                        bytes[0]
                });
            } else {
                bytes = ByteBuffer.allocate(4).putInt(0).array();
                outputStream.write(new byte[]{
                        bytes[3],
                        bytes[2],
                        bytes[1],
                        bytes[0]
                });
            }

            outputStream.write(new byte[]{
                    (byte) detectorIpAddr[0],
                    (byte) detectorIpAddr[1],
                    (byte) detectorIpAddr[2],
                    (byte) detectorIpAddr[3]
            });

            bytes = ByteBuffer.allocate(2).putShort((short) ipPort).array();
            outputStream.write(new byte[]{
                    bytes[1],
                    bytes[0]});

            bytes = ByteBuffer.allocate(2).putShort((short) ipPortExternal).array();
            outputStream.write(new byte[]{
                    bytes[1],
                    bytes[0]
            });

            return outputStream.toByteArray();
        }

        @Override
        public String toString() {
            return String.format("Причина: %s%s, IP адрес БД: %s, IP порт БД: %s, IP порт внешних устройств: %s%s",
                    reason.title,
                    param != null ? "/" + param.name() : "",
                    Arrays.toString(detectorIpAddr),
                    ipPort,
                    externalDeviceIpPort,
                    sourceIpAddr != null ? ", IP адрес источника команды перезапуска: " + Arrays.toString(sourceIpAddr) : "");
        }
    }

    public static class InitializationDetectorState extends SomeEvent {

        public InitializationDetectorState(int detectorID, int detectorTime) {
            super(detectorID, detectorTime, STATE, wrapToPackage(detectorID, detectorTime, STATE, getData()));
        }

        private static byte[] getData() {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(INITIALIZATION.code);

            return outputStream.toByteArray();
        }

        @Override
        public String toString() {
            return String.format("%s", INITIALIZATION.title);
        }
    }

    public static class ErrorDetectorState extends SomeEvent {

        private final Error error;

        public ErrorDetectorState(int detectorID, int detectorTime, Error error) {
            super(detectorID, detectorTime, STATE, wrapToPackage(detectorID, detectorTime, STATE, getData(error)));
            this.error = error;
        }

        @SneakyThrows
        private static byte[] getData(Error err) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(ERROR.code);
            outputStream.write(err.code);

            return outputStream.toByteArray();
        }


        @Override
        public String toString() {
            return String.format("%s. Ошибка: %s", ERROR.title, error.title);
        }
    }

    public static class AccumulationDetectorState extends SomeEvent {

        public final long curTime;

        public final long measTime;

        public AccumulationDetectorState(int detectorID, int detectorTime, long curTime, long measTime) {
            super(detectorID, detectorTime, STATE, wrapToPackage(detectorID, detectorTime, STATE, getData(curTime, measTime)));
            this.curTime = curTime;
            this.measTime = measTime;
        }

        @SneakyThrows
        private static byte[] getData(long curTime, long measTime) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(ACCUMULATION.code);
            byte[] bytes = ByteBuffer.allocate(4).putInt((int) curTime).array();
            outputStream.write(new byte[]{
                    bytes[3],
                    bytes[2],
                    bytes[1],
                    bytes[0]
            });

            bytes = ByteBuffer.allocate(4).putInt((int) measTime).array();
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
            return String.format("%s. Текущее время: %d, Время измерения: %d", ACCUMULATION.title, curTime, measTime);
        }
    }

    public static class MeasurementDetectorState extends SomeEvent {

        public final Measurement meas;

        public MeasurementDetectorState(int detectorID, int detectorTime, Measurement meas) {
            super(detectorID, detectorTime, STATE, wrapToPackage(detectorID, detectorTime, STATE, getData(meas)));
            this.meas = meas;
        }

        @SneakyThrows
        private static byte[] getData(Measurement meas) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(MEASUREMENT.code);
            outputStream.write(new byte[]{0x01, 0x00}); //structVersion
            outputStream.write(new byte[]{meas.bdType.code, 0x00});

            byte[] bytes = ByteBuffer.allocate(4).putInt((int) meas.measTime).array();
            outputStream.write(new byte[]{
                    bytes[3],
                    bytes[2],
                    bytes[1],
                    bytes[0]
            });

            bytes = ByteBuffer.allocate(4).putInt((int) meas.geoTime).array();
            outputStream.write(new byte[]{
                    bytes[3],
                    bytes[2],
                    bytes[1], bytes[0]
            });

            outputStream.write(new byte[]{0x08, 0x00}); //geoDataSize
            bytes = ByteBuffer.allocate(4).putFloat(meas.geoData.lat()).array();
            outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
            bytes = ByteBuffer.allocate(4).putFloat(meas.geoData.lon()).array();
            outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});

            bytes = ByteBuffer.allocate(4).putFloat(meas.bdData.getCurrentScore()).array();
            outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
            bytes = ByteBuffer.allocate(4).putFloat(meas.bdData.getAverageScore()).array();
            outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
            bytes = ByteBuffer.allocate(4).putFloat(meas.bdData.getCurrentMeasData()).array();
            outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
            bytes = ByteBuffer.allocate(4).putFloat(meas.bdData.getAverageMeasData()).array();
            outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});

            bytes = ByteBuffer.allocate(4).putInt((int) meas.bdData.getAccumulatedTime()).array();
            outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
            bytes = ByteBuffer.allocate(4).putFloat(meas.bdData.getAccumulatedMeasData()).array();
            outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
            bytes = ByteBuffer.allocate(4).putFloat(meas.bdData.getAccumulatedPowerMeasData()).array();
            outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});

            return outputStream.toByteArray();
        }

        @Override
        public String toString() {
            return String.format("%s. %s", MEASUREMENT.title, meas.toString());
        }
    }

    public static class InternalEvent extends SomeEvent {

        public final InternalData internalData;

        public InternalEvent(int detectorID, int detectorTime, InternalData internalData) {
            super(detectorID, detectorTime, INTERNAL_DATA, wrapToPackage(detectorID, detectorTime, INTERNAL_DATA,
                    getData(internalData)));
            this.internalData = internalData;
        }

        @Override
        public String toString() {
            return internalData.toString();
        }


        @SneakyThrows
        private static byte[] getData(InternalData internalData) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(new byte[]{0x09, 0x00}); //structVersion
            outputStream.write(new byte[]{internalData.bdType.code, 0x00});

            byte[] bytes = ByteBuffer.allocate(4).putInt((int) internalData.measTime).array();
            outputStream.write(new byte[]{
                    bytes[3],
                    bytes[2],
                    bytes[1],
                    bytes[0]
            });

            outputStream.write(new byte[]{BD_MODE_CONTINUOUS_HIGH_SENS.code, 0x00});

            outputStream.write(new byte[]{0x00, 0x00}); //reserve

            if (internalData.bdType == BDType.GAMMA) {
                bytes = ByteBuffer.allocate(4).putFloat(internalData.currentScores[0]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
                bytes = ByteBuffer.allocate(4).putFloat(internalData.currentScores[1]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});

                bytes = ByteBuffer.allocate(4).putFloat(internalData.averageScores[0]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
                bytes = ByteBuffer.allocate(4).putFloat(internalData.averageScores[1]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});

                bytes = ByteBuffer.allocate(4).putFloat(internalData.temperature).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});

                bytes = ByteBuffer.allocate(4).putFloat(((BdmgInternalData) internalData).voltage400V).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
            } else {
                bytes = ByteBuffer.allocate(4).putFloat(internalData.currentScores[0]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
                bytes = ByteBuffer.allocate(4).putFloat(internalData.currentScores[1]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
                bytes = ByteBuffer.allocate(4).putFloat(internalData.currentScores[2]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
                bytes = ByteBuffer.allocate(4).putFloat(internalData.currentScores[3]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});

                bytes = ByteBuffer.allocate(4).putFloat(internalData.averageScores[0]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
                bytes = ByteBuffer.allocate(4).putFloat(internalData.averageScores[1]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
                bytes = ByteBuffer.allocate(4).putFloat(internalData.averageScores[2]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
                bytes = ByteBuffer.allocate(4).putFloat(internalData.averageScores[3]).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});

                bytes = ByteBuffer.allocate(4).putFloat(internalData.temperature).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
                bytes = ByteBuffer.allocate(4).putFloat(((BdpnInternalData) internalData).voltage500V).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
                bytes = ByteBuffer.allocate(4).putFloat(((BdpnInternalData) internalData).voltage2500V).array();
                outputStream.write(new byte[]{bytes[3], bytes[2], bytes[1], bytes[0]});
            }

            return outputStream.toByteArray();
        }
    }

    public static class SomeEvent extends DetectorMsg {

        public final Event eventCode;

        public SomeEvent(int detectorID,
                         long time,
                         Event eventCode,
                         byte[] data) {
            super(detectorID, time, data);
            this.eventCode = eventCode;
        }
    }
}
