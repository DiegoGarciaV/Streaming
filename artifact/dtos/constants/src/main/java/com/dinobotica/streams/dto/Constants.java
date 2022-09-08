package com.dinobotica.streams.dto;

import java.time.LocalDateTime;

public class Constants {

    public static final int BUFFER_SIZE = 1024<<15;
    public static final int FRAME_RATE = 45;
    public static final int CHUNK_RATE = 15;
    public static final int START_PORT = 6600;

    private static final String TODAY_YEAR = "" + LocalDateTime.now().getYear();
    private static final String TODAY_MONTH = "" +  (LocalDateTime.now().getMonthValue()< 10 ? "0" + LocalDateTime.now().getMonthValue() : LocalDateTime.now().getMonthValue());
    private static final String TODAY = "" +  (LocalDateTime.now().getDayOfMonth() < 10 ? "0" + LocalDateTime.now().getDayOfMonth() : LocalDateTime.now().getDayOfMonth());
    
    public static final String FRAMES_PATH = "frames/tmp/" + TODAY_YEAR + TODAY_MONTH + TODAY + "/";
}
