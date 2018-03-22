package com.dev.eipeks.lecteur.core_package.dagger.modules.viewmodels;

import com.dev.eipeks.lecteur.screen.viewmodel.MainVM;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eipeks on 3/19/18.
 */
@Module
public class MainVMModule {

    @Provides
    public MainVM provideMainVM(){
        return new MainVM();
    }

}
