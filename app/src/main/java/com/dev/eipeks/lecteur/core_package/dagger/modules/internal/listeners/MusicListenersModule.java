package com.dev.eipeks.lecteur.core_package.dagger.modules.internal.listeners;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.dev.eipeks.lecteur.core_package.dagger.modules.internal.connection.ConnectionModule;
import com.dev.eipeks.lecteur.core_package.model.MainBinder;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eipeks on 3/23/18.
 */

@Module(includes = {ConnectionModule.class})
public class MusicListenersModule {

    private MainBinder binder;

    public MusicListenersModule(MainBinder binder){
        this.binder = binder;
    }

    @Provides
    public MediaPlayer.OnPreparedListener provideOnPreparedListener(){
        return new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        };
    }

    @Provides
    public MediaPlayer.OnCompletionListener provideOnCompletionListener(){
        return new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (mp.getCurrentPosition() > 0){
                    mp.reset();
                    binder.getService().playNext();
                }
            }
        };
    }

    @Provides
    public MediaPlayer.OnErrorListener provideOnErrorListener(){
        return new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mp.reset();
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
