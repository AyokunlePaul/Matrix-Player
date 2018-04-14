package com.dev.eipeks.matrixplayer.core.dagger.modules.viewmodels;

import com.dev.eipeks.matrixplayer.core.dagger.scope.MainScope;
import com.dev.eipeks.matrixplayer.core.store.OfflineStore;
import com.dev.eipeks.matrixplayer.screen.viewmodel.MainVM;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eipeks on 3/19/18.
 */
@Module
public class MainVMModule {

    @Provides
    @MainScope
    public MainVM provideMainVM(OfflineStore offlineStore){
        return new MainVM(offlineStore);
    }

}
