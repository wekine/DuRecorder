package com.example.nurserecorder.core;


public interface OnDecodeListener {

    void onImageDecoded(byte[] image);

    void onSampleDecoded(byte[] sample);

    void onDecodeEnded(boolean vsucceed, boolean asucceed);

}
