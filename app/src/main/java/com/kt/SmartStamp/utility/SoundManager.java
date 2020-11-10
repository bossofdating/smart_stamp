package com.kt.SmartStamp.utility;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

/**
 * 사운드(알림음) 재생 관리 클래스
 */
public class SoundManager {

    private static SoundManager sInstance;
    private SoundPool soundPool;
    private Context context;

    private SoundManager(Context context) {
        init(context);
    }

    public static SoundManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SoundManager(context);
        }
        return sInstance;
    }

    //초기화 하기
    public void init(Context context) {
        this.context = context;
        soundPool = new SoundPool(10, AudioManager.STREAM_ALARM, 0);
    }

    /**
     * 리소스 재생
     * @param rawResource
     */
    public void play(final int rawResource, final float volume) {

        soundPool.load(context, rawResource, 1);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                soundPool.play(sampleId,  volume, volume, 1, 0, 1);
            }
        });
    }

    /**
     * 리소스 재생 중지
     * @param rawResource
     */
    public void stopSound(int rawResource) {
        soundPool.stop(rawResource);
    }

}
