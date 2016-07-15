package com.example.pedometer.igo.Utils;

import android.app.Service;
import android.speech.tts.TextToSpeech;
import android.text.format.Time;
import android.util.Log;

import java.util.Locale;

public class Utils implements TextToSpeech.OnInitListener{
    private static final String TAG = "Utils";
    private Service mService;

    private static Utils instance = null;

    private Utils() {
    }

    public static Utils getInstance() {
        if(instance==null) {
            instance = new Utils();
        }
        return instance;
    }

    public void setService(Service service) { mService = service;}

    private TextToSpeech mTts;
    private boolean mSpeak = false;
    private boolean mSpeakingEngineAvailable = false;

    public void initTTS() {
        mTts = new TextToSpeech(mService,this);
    }

    public void shutdownTTS() {
        mSpeakingEngineAvailable = false;
        mTts.shutdown();

    }

    public void say(String text) {
        if(mSpeak && mSpeakingEngineAvailable) {
            mTts.speak(text,TextToSpeech.QUEUE_ADD,null);
        }
    }
    @Override
    public void onInit(int status) {
        if(status==TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.CHINA);
            if(result==TextToSpeech.LANG_MISSING_DATA||result==TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not available");
            } else {
                Log.i(TAG,"TextToSpeech Initialized.");
                mSpeakingEngineAvailable = true;
            }
        } else {
                Log.e(TAG, "failed");
        }
    }

    public void setSpeak(boolean speak) {mSpeak = speak;}

    public boolean isSpeakingEnabled() {return mSpeak;}

    public boolean isSpeakingNow() {return mTts.isSpeaking();}

    public void ding() {
    }

    public static long currentTimeInMillis() {
        Time time = new Time();
        time.setToNow();
        return time.toMillis(false);
    }
}
