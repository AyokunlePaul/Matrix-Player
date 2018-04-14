package com.dev.eipeks.matrixplayer.core.store;

import android.content.Context;
import android.content.SharedPreferences;

import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.dev.eipeks.matrixplayer.global.AppState;
import com.dev.eipeks.matrixplayer.global.Constants;
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

    private void initSharedPreferences(){
        preferences = context.getSharedPreferences("Matrix Player", Context.MODE_PRIVATE);
        editor = preferences.edit();
        editor.apply();
    }

    public void cacheCurrentSong(SongModel model){
        String modelString = new Gson().toJson(model);
        editor.putString(Constants.CONSTANT_LAST_PLAYED, modelString);
        editor.apply();
    }

    public SongModel getLastSongPlayed(){
        String lastPlayed = preferences.getString(Constants.CONSTANT_LAST_PLAYED, null);
        return new Gson().fromJson(lastPlayed, SongModel.class);
    }

    public void setLastSongPlayedPosition(int position){
        editor.putInt(Constants.CONSTANT_LAST_SONG_POSITION_PLAYED, position);
        editor.apply();
    }

    public int getLastSongPlayedPosition(){
        return preferences.getInt(Constants.CONSTANT_LAST_SONG_POSITION_PLAYED, 0);
    }

    public void setCurrentAppState(AppState.APP_STATE appState){
        String state = new Gson().toJson(appState);
        editor.putString(Constants.CONSTANT_APP_STATE, state);
        editor.apply();
    }

    public AppState.APP_STATE getAppState(){
        String appState = preferences.getString(Constants.CONSTANT_APP_STATE, null);
        return new Gson().fromJson(appState, AppState.APP_STATE.class);
    }

    public void setCurrentViewState(AppState.CURRENT_VIEW_STATE currentViewState){
        String state = new Gson().toJson(currentViewState);
        editor.putString(Constants.CONSTANT_CURRENT_VIEW_STATE, state);
        editor.apply();
    }

    public AppState.CURRENT_VIEW_STATE getCurrentViewState(){
        String currentViewState = preferences.getString(Constants.CONSTANT_CURRENT_VIEW_STATE, null);
        return new Gson().fromJson(currentViewState, AppState.CURRENT_VIEW_STATE.class);
    }

    public void setLastSongDuration(long duration){
        editor.putLong(Constants.CONSTANT_LAST_SONG_DURATION, duration);
        editor.apply();
    }

    public Long getLastSongDuration(){
        return preferences.getLong(Constants.CONSTANT_LAST_SONG_DURATION, 0);
    }

}
