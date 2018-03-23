package com.dev.eipeks.lecteur.core_package.dagger.modules.internal.controller;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.widget.MediaController;

import com.dev.eipeks.lecteur.core_package.dagger.modules.external.ContextModule;
import com.dev.eipeks.lecteur.core_package.dagger.modules.internal.listeners.MusicListenersModule;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eipeks on 3/19/18.
 */
@Module (includes = {ContextModule.class, MusicListenersModule.class})
public class MusicControllerModule {

    @Provides
    public MediaPlayer provideMediaPlayer(Context context,
                                          MediaPlayer.OnPreparedListener preparedListener,
                                          MediaPlayer.OnCompletionListener completionListener,
                                          MediaPlayer.OnErrorListener errorListener){
        MediaPlayer player = new MediaPlayer();
        player.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnCompletionListener(completionListener);
        player.setOnErrorListener(errorListener);
        player.setOnPreparedListener(preparedListener);

        return player;
    }

    @Provides
    public MediaController provideMediaController(Context context){
        return new MediaController(context);
    }

}
