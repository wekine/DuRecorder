package com.example.nurserecorder.core;


import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;

import com.example.nurserecorder.common.Constant;
import com.example.nurserecorder.utils.DuFileUtils;

import java.io.File;
import java.nio.ByteBuffer;

class DuRecorder {

    private static final String TAG = "DuRecorder";

    private static final long DEFAULT_TIMEOUT = 10 * 1000;

    private MediaMuxer mMediaMuxer;
    private MediaCodec mVideoEncoder;
    private MediaCodec mAudioEncoder;
    private MediaCodec.BufferInfo mVBufferInfo;
    private MediaCodec.BufferInfo mABufferInfo;

    private boolean mIsInitialized = false;
    private long mVStartTime;
    private long mAStartTime;
    private long mVLastPts;
    private long mALastPts;
    private int mVTrackIndex;
    private int mATrackIndex;
    private volatile boolean mMuxerStarted;

    void init(int width, int height, int colorFormat, int bitRate, int sampleRate,
              int channels) throws Exception {

        if (getCodecInfo(DuCodec.MIME_TYPE_AVC) == null || getCodecInfo(DuCodec.MIME_TYPE_AAC) == null) {
            throw new Exception("cannot find suitable codec");
        }

        MediaFormat videoFormat = MediaFormat.createVideoFormat(DuCodec.MIME_TYPE_AVC, width, height);
        videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, Constant.DEFAULT_FRAME_RATE);
        videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Constant.DEFAULT_I_FRAME_INTERVAL);
        videoFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);

        mVideoEncoder = MediaCodec.createEncoderByType(DuCodec.MIME_TYPE_AVC);
        mVideoEncoder.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mVideoEncoder.start();

        MediaFormat audioFormat = MediaFormat.createAudioFormat(DuCodec.MIME_TYPE_AAC, sampleRate, channels);
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, Constant.DEFAULT_BITRATE_AUDIO);

        mAudioEncoder = MediaCodec.createEncoderByType(DuCodec.MIME_TYPE_AAC);
        mAudioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioEncoder.start();

        // 文件输出路径不提供外面设置能力
        String getOutputMediaDir = DuFileUtils.getOutputMediaDir(DuFileUtils.MEDIA_TYPE_VIDEO);
        assert getOutputMediaDir != null;
        File file = new File(getOutputMediaDir);
        if (file.exists() && !file.delete()) {
            Log.w(TAG, "delete file failed");
        }

        mMediaMuxer = new MediaMuxer(getOutputMediaDir, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mMuxerStarted = false;
        mVTrackIndex = -1;
        mATrackIndex = -1;
        mVStartTime = -1;
        mAStartTime = -1;
        mVLastPts = -1;
        mALastPts = -1;

        mVBufferInfo = new MediaCodec.BufferInfo();
        mABufferInfo = new MediaCodec.BufferInfo();
        mIsInitialized = true;
        Log.i(TAG, "Recorder initialized");
    }

    private static MediaCodecInfo getCodecInfo(final String mimeType) {
        final int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            final MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            final String[] types = codecInfo.getSupportedTypes();
            for (String type : types) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("WeakerAccess")
    public void recordImage(byte[] image) throws Exception {
        long pts;
        if (mVStartTime == -1) {
            mVStartTime = System.nanoTime();
            pts = 0;
        } else {
            pts = (System.nanoTime() - mVStartTime) / 1000;
        }
        if (pts <= mVLastPts) {
            pts += (mVLastPts - pts) + 1000;
        }
        mVLastPts = pts;
        doRecord(mVideoEncoder, mVBufferInfo, image, pts);
    }

    @SuppressWarnings("WeakerAccess")
    public void recordSample(byte[] sample) throws Exception {
        long pts;
        if (mAStartTime == -1) {
            mAStartTime = System.nanoTime();
            pts = 0;
        } else {
            pts = (System.nanoTime() - mAStartTime) / 1000;
        }
        if (pts <= mALastPts) {
            pts += (mALastPts - pts) + 1000;
        }
        mALastPts = pts;
        doRecord(mAudioEncoder, mABufferInfo, sample, pts);
    }

    private void doRecord(MediaCodec encoder, MediaCodec.BufferInfo bufferInfo, byte[] data,
                          long pts) throws Exception {
        if (!mIsInitialized) {
            Log.e(TAG, "Recorder must be initialized!");
            return;
        }
        int index = encoder.dequeueInputBuffer(DEFAULT_TIMEOUT);
        ByteBuffer[] inputBuffers = encoder.getInputBuffers();
        ByteBuffer buffer = inputBuffers[index];
        if (index >= 0) {
            buffer.clear();
            buffer.put(data);
            encoder.queueInputBuffer(index, 0, data.length, pts, 0);
        }
        drainEncoder(encoder, bufferInfo);
    }

    private void drainEncoder(MediaCodec encoder, MediaCodec.BufferInfo bufferInfo) throws Exception {
        int trackIndex = encoder == mVideoEncoder ? mVTrackIndex : mATrackIndex;
        ByteBuffer[] outputBuffers = encoder.getOutputBuffers();
        while (true) {
            int index = encoder.dequeueOutputBuffer(bufferInfo, DEFAULT_TIMEOUT);
            if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = encoder.getOutputBuffers();
            } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                trackIndex = addTrackIndex(encoder);
            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (index < 0) {
                Log.w(TAG, "drainEncoder unexpected result: " + index);
            } else {
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    continue;
                }

                if (bufferInfo.size != 0) {
                    ByteBuffer outputBuffer = outputBuffers[index];

                    if (outputBuffer == null) {
                        throw new RuntimeException("drainEncoder get outputBuffer " + index + " was null");
                    }

                    synchronized (this) {
                        if (!mMuxerStarted) {
                            wait();
                        }
                    }

                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                    mMediaMuxer.writeSampleData(trackIndex, outputBuffer, bufferInfo);
                }

                encoder.releaseOutputBuffer(index, false);
            }
        }
    }

    private int addTrackIndex(MediaCodec encoder) {
        int trackIndex;
        synchronized (this) {
            MediaFormat format = encoder.getOutputFormat();
            if (DuCodec.getMediaType(format) == DuCodec.MEDIA_TYPE_VIDEO) {
                mVTrackIndex = mMediaMuxer.addTrack(format);
                trackIndex = mVTrackIndex;
            } else {
                mATrackIndex = mMediaMuxer.addTrack(format);
                trackIndex = mATrackIndex;
            }

            if (mVTrackIndex != -1 && mATrackIndex != -1) {
                mMediaMuxer.start();
                mMuxerStarted = true;
                notifyAll();
                Log.i(TAG, "MediaMuxer has added all track, notifyAll");
            }
        }
        return trackIndex;
    }

    public void stop() {
        try {
            release();
        } catch (Exception e) {
            Log.e(TAG, "stop exception occur: " + e.getLocalizedMessage());
        }
        if (mIsInitialized) {
            Log.i(TAG, "Recorder released");
        }
        mIsInitialized = false;
    }

    private void release() throws Exception {
        if (mVideoEncoder != null) {
            mVideoEncoder.stop();
            mVideoEncoder.release();
            mVideoEncoder = null;
        }

        if (mAudioEncoder != null) {
            mAudioEncoder.stop();
            mAudioEncoder.release();
            mAudioEncoder = null;
        }

        if (mMediaMuxer != null) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
        }
    }


}
