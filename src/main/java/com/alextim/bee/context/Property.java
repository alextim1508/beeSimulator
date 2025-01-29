package com.alextim.bee.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Property {
    public static String TITLE_APP;
    public static String SOFTWARE_VERSION = "Версия ПО: 1.1";
    public static String FRONTEND_FOR_DETECTOR;
    public static Integer TRANSFER_TO_DETECTOR_ID;
    public static String TRANSFER_IP;
    public static Integer TRANSFER_RCV_PORT;
    public static Integer TRANSFER_TR_PORT;
    public static Integer TRANSFER_RCV_BUFFER_SIZE;

    public static int[] DETECTOR_IP_ADDR = new int[]{127, 0, 0, 1};
    public static int[] SOURCE_IP_ADDR = new int[]{127, 0, 0, 1};

    public static String COUNTER_NUMBER_FORMAT;
    public static String MEAS_DATA_NUMBER_FORMAT;
    public static String OTHER_NUMBER_FORMAT;
    public static Integer MEAS_DATA_NUMBER_SING_DIGITS;
}
