package com.dev.eipeks.matrixplayer.service;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dev.eipeks.matrixplayer.MainApplication;
import com.dev.eipeks.matrixplayer.core.dagger.component.MainComponent;
import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.dev.eipeks.matrixplayer.core.store.OfflineStore;
import com.dev.eipeks.matrixplayer.screen.viewmodel.MainVM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainService extends Service {

    private MainComponent component;

    private int currentSongPosition = 0;

    private List<SongModel> songs = new ArrayList<>();

    MediaPlayer player;

    @Inject
    MainVM mainVM;

    @Inject
    OfflineStore store;

    @Override
    public void onCreate() {

        component = MainApplication.get(this).getComponent();
        component.inject(this);

        initializePlayer();

        Log.i("Lecteur: Player", String.valueOf(songs.size()));

        super.onCreate();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Lecteur: Service", "Activity bounded");
        return new MainBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();

        return true;
    }

    private void initializePlayer(){
        player = new MediaPlayer();
        player.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (player.getCurrentPosition() >= 0){
                    mp.reset();
                    playNext();
                }
            }
        });
        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    public void play(int position){
        this.currentSongPosition = position;
        playSong();
    }

    private void playSong(){
        player.reset();

        SongModel currentSong = songs.get(currentSongPosition);

        store.cacheCurrentSong(currentSong);

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

    public void setSongs(List<SongModel> model){
        this.songs = model;
        Log.i("Matrix Player: Service", String.valueOf(model.size()));
    }

    public class MainBinder extends Binder {
        public MainService getService(){
            return MainService.this;
        }
    }
}
