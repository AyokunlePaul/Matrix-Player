package com.dev.eipeks.lecteur.core_package.dagger.modules.internal.manager;

import android.content.Context;
import android.media.AudioManager;

import com.dev.eipeks.lecteur.core_package.dagger.modules.external.ContextModule;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eipeks on 3/23/18.
 */

@Module(includes = {ContextModule.class})
public class ManagerModule {

    @Provides
    public AudioManager provideAudioManager(Context context){
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

}
