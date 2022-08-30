package com.dinobotica.streams.dto;

import java.time.LocalDateTime;

public class Constants {

    public static final int BUFFER_SIZE = 1024<<9;
    public static final int FRAME_RATE = 120;
    public static final String FRAMES_PATH = "frames/tmp/" + LocalDateTime.now().getYear() + (LocalDateTime.now().getMonthValue()<10 ? "0" + LocalDateTime.now().getMonthValue() : LocalDateTime.now().getMonthValue()) + LocalDateTime.now().getDayOfMonth() + "/";
}
