package com.dev.eipeks.matrixplayer.core.dagger.component;

import com.dev.eipeks.matrixplayer.core.dagger.modules.external.ContextModule;
import com.dev.eipeks.matrixplayer.core.dagger.modules.internal.OfflineStoreModule;
import com.dev.eipeks.matrixplayer.core.dagger.modules.viewmodels.MainVMModule;
import com.dev.eipeks.matrixplayer.core.dagger.scope.MainScope;
import com.dev.eipeks.matrixplayer.screen.activity.MainActivity;
import com.dev.eipeks.matrixplayer.service.MainService;

import dagger.Component;

/**
 * Created by eipeks on 3/19/18.
 */

@Component(modules = {ContextModule.class, MainVMModule.class, OfflineStoreModule.class})
@MainScope
public interface MainComponent {

    void inject(MainService mainService);
    void inject(MainActivity mainActivity);

}
