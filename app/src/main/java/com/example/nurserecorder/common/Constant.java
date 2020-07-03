package com.example.nurserecorder.common;

import android.media.AudioFormat;

public class Constant {

    /**
     * 音频采样频率，每秒钟采样的次数，采样率越高，有音质越高 官方推荐值：44100、22050、11025
     */
    public static final int DEFAULT_SAMPLE_RATE = 44100;
    /**
     * PCM编码格式和采样大小 android支持的采样大小16bit或8bit，越大，信息量越多，音质也越高，主流的采样大小是16bit
     */
    public static final int DEFAULT_PCM_DATA_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * android支持双声道立体声和单声道 MONO单声道，STEREO立体声,默认　单声道,效率更高
     */
    public static final int DEFAULT_CHANNELS = 1;
    /**
     * 音频码率 默认 128k
     */
    public static final int DEFAULT_BITRATE_AUDIO = 128 * 1000;

    /**
     * 帧率 默认15帧
     */
    public static final int DEFAULT_FRAME_RATE = 15;
    /**
     * 关键帧的间隔数量
     */
    public static final int DEFAULT_I_FRAME_INTERVAL = 5;
    /**
     * 视频码率 1200k,这个是RTC之前设置的一个码率值
     */
    public static final int DEFAULT_BITRATE_VIDEO = 1200 * 1000;

    public static final float[] ASPECT_RATIO_ARRAY = {9.0f / 16, 16.0f / 9};
}
