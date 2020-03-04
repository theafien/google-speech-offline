package com.google.speech.recognizer;

import android.util.Log;

import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.UnknownFieldSetLite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class AbstractRecognizer {

    private static final Logger logger = Logger.getLogger(AbstractRecognizer.class.getName());

    static public String TAG = "AbstractRecognizer";

    public InputStream reader;
    private long nativeObj;

    FileOutputStream lhandleEndpointerEvent;
    FileOutputStream lhandleRecognitionEvent;

    static {

    }

    private native int nativeCancel(final long nativeObj);

    private native long nativeConstruct();

    private native void nativeDelete(final long nativeObj);

    private native int nativeInitFromProto(final long nativeObj, final long resourceNativeObj, final byte[] config);

    private native byte[] nativeRun(final long nativeObj, final byte[] params);

    public AbstractRecognizer() {
        this.nativeObj = this.nativeConstruct();
    }

    private final void validate() {
        if (this.nativeObj != 0L) {
            return;
        }
        throw new IllegalStateException("recognizer is not initialized");
    }

    public final void run(final byte[] params) {
        this.validate();
        final byte[] nativeRun = this.nativeRun(this.nativeObj, params);

    }

    public final int init(final byte[] array, final ResourceManager resourceManager) {
        this.validate();
        return this.nativeInitFromProto(
                this.nativeObj,
                resourceManager.nativeObj,
                array
        );
    }

    public final void delete() {
        synchronized (this) {
            if (this.nativeObj != 0L) {
                this.nativeDelete(this.nativeObj);
                this.nativeObj = 0L;
            }
        }
    }

    public final int cancel() {
        this.validate();
        //return fgg.instanceId(this.nativeCancel(this.instanceId));
        return this.nativeCancel(this.nativeObj);
    }

    @Override
    protected void finalize() {
        this.delete();
    }

    protected void handleAudioLevelEvent(final byte[] array) {
        UnknownFieldSet set = null;
        try {
            set = UnknownFieldSet.parseFrom(array);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        Log.d(TAG, String.format("handleAudioLevelEvent: %s", set.toString()));
    }

    protected void handleEndpointerEvent(final byte[] array) {

        try {
            lhandleEndpointerEvent.write("-next-".getBytes());
            lhandleEndpointerEvent.write(array);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //https://stackoverflow.com/questions/7914034/how-to-decode-protobuf-binary-response
        try {
            Any any = Any.parseFrom(array);
            UnknownFieldSet set = UnknownFieldSet.parseFrom(array);

            Log.d(TAG, String.format("handleEndpointerEvent: %s", set.toString()));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    protected void handleHotwordEvent(final byte[] array) {
        UnknownFieldSet set = null;
        try {
            set = UnknownFieldSet.parseFrom(array);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        Log.d(TAG, String.format("handleHotwordEvent: %s", set.toString()));
    }

    protected void handleRecognitionEvent(final byte[] array) {

        try {
            lhandleRecognitionEvent.write("-next-".getBytes());
            lhandleRecognitionEvent.write(array);
        } catch (IOException e) {
            e.printStackTrace();
        }

        UnknownFieldSet set = null;
        try {
            set = UnknownFieldSet.parseFrom(array);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        Log.d(TAG, String.format("handleRecognitionEvent: %s", set.toString()));

    }

    protected int read(final byte[] buffer) {

        Log.d(TAG, String.format("AbstractRecognizer.read(%d)", buffer.length));

        if (buffer.length > 0) {
            int bytesRead = 0;
            try {
                bytesRead = this.reader.read(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return bytesRead == -1 ? 0 : bytesRead;
        }
        return -1;
    }

    protected void setAudioReader(InputStream audioStream) {
        reader = audioStream;
    }

    public void setLogFile(File sdcard) {

        try {
            lhandleEndpointerEvent = new FileOutputStream(new File (sdcard.getAbsolutePath() + String.format("/theafien/endpointer_%d.bin", this.nativeObj)));
            lhandleRecognitionEvent = new FileOutputStream(new File (sdcard.getAbsolutePath() + String.format("/theafien/recognition_%d.bin", this.nativeObj)));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
