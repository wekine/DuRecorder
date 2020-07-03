package com.example.nurserecorder;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Camera;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.nurserecorder.common.Constant;
import com.example.nurserecorder.core.DuAudioRecorder;
import com.example.nurserecorder.core.DuRecorderWrapper;
import com.example.nurserecorder.view.DuSurfaceView;
import com.example.nurserecorder.view.PreviewCallback;

public class RecordActivity extends AppCompatActivity implements DuAudioRecorder.AudioRecordCallback,
        PreviewCallback, Camera.PreviewCallback {

    private Button mStartButton;
    private Button mStopButton;

    private DuRecorderWrapper mRecorder = new DuRecorderWrapper();
    private DuAudioRecorder mDuAudioRecorder = new DuAudioRecorder();
    private Camera.Size mPreviewSize;
    private int mPreviewFormat;
    private volatile boolean mIsRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DuSurfaceView mDuSurfaceView = new DuSurfaceView(RecordActivity.this);
        mStartButton = this.findViewById(R.id.start_record);
        mStopButton = this.findViewById(R.id.stop_record);
        mStartButton.setOnClickListener(v -> startRecord());
        mStopButton.setOnClickListener(v -> stopRecord());

        mDuSurfaceView.setPreviewCallback(this);
        mDuSurfaceView.openCamera();

        initData();
    }

    private void initData() {
        mDuAudioRecorder.setSampleRate(Constant.DEFAULT_SAMPLE_RATE);
        mDuAudioRecorder.setRecordCallback(this);
    }

    private void startRecord() {
        boolean succeed = mRecorder.init(mPreviewSize.width, mPreviewSize.height, mPreviewFormat,
                Constant.DEFAULT_BITRATE_VIDEO, Constant.DEFAULT_SAMPLE_RATE, Constant.DEFAULT_CHANNELS);
        if (succeed) {
            disableButtons();
            mStopButton.postDelayed(() -> mStopButton.setEnabled(true), 3000);
            mIsRecording = true;
            mDuAudioRecorder.start();
        } else {
            Toast.makeText(DuApplication.getContext(), "创建硬件编码器失败", Toast.LENGTH_LONG).show();
        }
    }

    private void stopRecord() {
        mIsRecording = false;
        resetButtons();
        mDuAudioRecorder.stop();
        mRecorder.stop();
        Toast.makeText(DuApplication.getContext(), "视频正在处理中", Toast.LENGTH_LONG).show();
    }

    private void disableButtons() {
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(false);
    }

    private void resetButtons() {
        mStartButton.setEnabled(true);
        mStopButton.setEnabled(true);
    }

    @Override
    public void onRecordSample(byte[] data) {
        if (mIsRecording) {
            mRecorder.recordSample(data);
        }
    }

    @Override
    public void onPreviewStarted(Camera camera) {
        // 如果没有下面这一句,录出来的视频是空的
        mPreviewSize = camera.getParameters().getPreviewSize();
        mPreviewFormat = camera.getParameters().getPreviewFormat();
        camera.setPreviewCallback(this);
    }

    @Override
    public void onPreviewStopped() {

    }

    @Override
    public void onPreviewFailed() {

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mIsRecording) {
            mRecorder.recordImage(data);
        }
    }
}
