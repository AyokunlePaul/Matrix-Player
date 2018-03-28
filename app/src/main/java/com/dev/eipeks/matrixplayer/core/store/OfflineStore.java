package com.dev.eipeks.matrixplayer.core.store;

import android.content.Context;
import android.content.SharedPreferences;

import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.google.gson.Gson;

import javax.inject.Inject;

/**
 * Created by eipeks on 3/28/18.
 */

public class OfflineStore {

    private Context context;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    @Inject
    public OfflineStore(Context context){
        this.context = context;
        initSharedPreferences();
    }

    public void cacheCurrentSong(SongModel model){
        String modelString = new Gson().toJson(model);
        editor.putString("Last Played", modelString);
        editor.apply();
    }

    public SongModel getLastPlayed(){
        String lastPlayed = preferences.getString("Last Played", null);
        return new Gson().fromJson(lastPlayed, SongModel.class);
    }

    private void initSharedPreferences(){
        preferences = context.getSharedPreferences("Matrix Player", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.apply();
    }

}
