package com.dev.eipeks.matrixplayer.core.dagger.modules.internal;

import android.content.Context;

import com.dev.eipeks.matrixplayer.core.dagger.scope.MainScope;
import com.dev.eipeks.matrixplayer.core.store.OfflineStore;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eipeks on 3/28/18.
 */
@Module
public class OfflineStoreModule {

    @Provides
    @MainScope
    public OfflineStore provideOfflineStore(Context context){
        return new OfflineStore(context);
    }

}
