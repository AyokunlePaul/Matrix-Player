package com.dev.eipeks.lecteur.core_package.dagger.component;

import com.dev.eipeks.lecteur.core_package.dagger.modules.internal.MusicControllerModule;
import com.dev.eipeks.lecteur.core_package.dagger.modules.viewmodels.MainVMModule;
import com.dev.eipeks.lecteur.screen.activity.Main;
import com.dev.eipeks.lecteur.service.MusicService;

import dagger.Component;

/**
 * Created by eipeks on 3/19/18.
 */

@Component(modules = {MusicControllerModule.class, MainVMModule.class})
public interface MainComponent {

    void inject(MusicService musicService);
    void inject(Main main);

}
