package com.dev.eipeks.matrixplayer.core.dagger.modules.external;

import android.content.Context;

import com.dev.eipeks.matrixplayer.core.dagger.scope.MainScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eipeks on 3/19/18.
 */
@Module
public class ContextModule {

    private Context context;

    public ContextModule(Context context){
        this.context = context;
    }

    @Provides
    @MainScope
    public Context provideContext(){
        return this.context;
    }

}
