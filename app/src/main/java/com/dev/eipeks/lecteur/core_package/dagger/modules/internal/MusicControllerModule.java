package com.dev.eipeks.lecteur.core_package.dagger.modules.internal;

import android.content.Context;
import android.media.MediaPlayer;
import android.widget.MediaController;

import com.dev.eipeks.lecteur.core_package.dagger.modules.external.ContextModule;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eipeks on 3/19/18.
 */
@Module (includes = {ContextModule.class})
public class MusicControllerModule {

    @Provides
    public MediaPlayer provideMediaPlayer(){
        return new MediaPlayer();
    }

    @Provides
    public MediaController provideMediaController(Context context){
        return new MediaController(context);
    }

}
