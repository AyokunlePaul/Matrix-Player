package com.dev.eipeks.lecteur.core_package.dagger.component;

import com.dev.eipeks.lecteur.core_package.dagger.modules.internal.connection.ConnectionModule;
import com.dev.eipeks.lecteur.core_package.dagger.modules.internal.controller.MusicControllerModule;
import com.dev.eipeks.lecteur.core_package.dagger.modules.internal.listeners.MusicListenersModule;
import com.dev.eipeks.lecteur.core_package.dagger.modules.internal.manager.ManagerModule;
import com.dev.eipeks.lecteur.core_package.dagger.modules.viewmodels.MainVMModule;
import com.dev.eipeks.lecteur.screen.activity.MainActivity;
import com.dev.eipeks.lecteur.service.MainService;

import dagger.Component;

/**
 * Created by eipeks on 3/19/18.
 */

@Component(modules = {MusicControllerModule.class, MusicListenersModule.class,
        ManagerModule.class, MainVMModule.class, ConnectionModule.class})
public interface MainComponent {

    void inject(MainService mainService);
    void inject(MainActivity mainActivity);

}
