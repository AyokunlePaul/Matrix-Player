package com.dev.eipeks.lecteur.service;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dev.eipeks.lecteur.MainApplication;
import com.dev.eipeks.lecteur.core_package.dagger.component.MainComponent;
import com.dev.eipeks.lecteur.core_package.model.MainBinder;
import com.dev.eipeks.lecteur.core_package.model.SongModel;
import com.dev.eipeks.lecteur.screen.viewmodel.MainVM;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainService extends Service {

    private MainComponent component;

    private int currentSongPosition = 0;

    private List<SongModel> songs;

    @Inject
    MediaPlayer player;
    @Inject
    MainVM mainVM;
    @Inject
    MainBinder binder;

    @Override
    public void onCreate() {

        component = MainApplication.get(this).getComponent();
        component.inject(this);

        songs = mainVM.getSongs(MainApplication.get(this));

        super.onCreate();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();

        return true;
    }

    private void playSong(){
        player.reset();

        SongModel currentSong = songs.get(currentSongPosition);
        long _id = currentSong._id;

        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, _id);

        try {
            Log.d("Lecteur: DATA SOURCE", "Setting data source");
            player.setDataSource(MainApplication.get(this), songUri);
        } catch (IOException e) {
            Log.e("Lecteur: DATA SOURCE", e.getLocalizedMessage());
            e.printStackTrace();
        }
        player.prepareAsync();
    }

    public void playNext(){
        currentSongPosition++;
        if (currentSongPosition >= songs.size()){
            currentSongPosition = 0;
            playSong();
        }
    }

    public void playPrevious(){
        currentSongPosition--;
        if (currentSongPosition <= 0){
            currentSongPosition = songs.size() - 1;
            playSong();
        }
    }

}
