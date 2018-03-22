package com.dev.eipeks.lecteur.core_package.dagger.modules.external;

import android.content.Context;

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
    public Context provideContext(){
        return this.context;
    }

}
