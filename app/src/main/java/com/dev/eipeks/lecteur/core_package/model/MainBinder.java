package com.dev.eipeks.lecteur.core_package.model;

import android.os.Binder;

import com.dev.eipeks.lecteur.service.MainService;

/**
 * Created by eipeks on 3/23/18.
 */

public class MainBinder extends Binder{

    public MainService getService(){
        return new MainService();
    }

}
