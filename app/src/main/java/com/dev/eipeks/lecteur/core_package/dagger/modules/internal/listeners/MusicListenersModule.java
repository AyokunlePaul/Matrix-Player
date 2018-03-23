package com.dev.eipeks.lecteur.core_package.dagger.modules.internal.listeners;

import android.media.AudioManager;
import android.media.MediaPlayer;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eipeks on 3/23/18.
 */

@Module
public class MusicListenersModule {

    @Provides
    public MediaPlayer.OnPreparedListener provideOnPreparedListener(){
        return new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.prepareAsync();
            }
        };
    }

    @Provides
    public MediaPlayer.OnCompletionListener provideOnCompletionListener(){
        return new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                mp.
            }
        };
    }

    @Provides
    public MediaPlayer.OnErrorListener provideOnErrorListener(){
        return new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        };
    }

    public AudioManager.OnAudioFocusChangeListener provideAudioFocusChangedListener(){
        return new AudioManager.OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                switch (focusChange){
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        break;
                }
            }
        };
    }

}
