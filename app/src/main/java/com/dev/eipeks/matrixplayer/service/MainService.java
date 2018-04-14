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
import com.dev.eipeks.matrixplayer.global.AppState;
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

    public List<SongModel> songs = new ArrayList<>();

    public MediaPlayer mediaPlayer;

    private MainVM.OnSongPlayedListener onSongPlayedListener;
    private MainVM.OnSongPausedListener onSongPausedListener;
    private MainVM.OnSongStoppedListener onSongStoppedListener;

    private boolean needsToSeek = false;

    @Inject
    MainVM mainVM;

    @Override
    public void onCreate() {

        component = MainApplication.get(this).getComponent();
        component.inject(this);

        initializePlayer();

//        mediaPlayer.getCurrentPosition();

        Log.i("Song List size", String.valueOf(songs.size()));

        super.onCreate();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Service", "Activity bounded");
        songs = mainVM.getSongs();
        return new MainBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mainVM.setCurrentAppState(AppState.APP_STATE.NOT_PLAYING);
        mainVM.setLastSongPlayedDuration(mediaPlayer.getCurrentPosition());
        mediaPlayer.stop();
        mediaPlayer.release();

        return true;
    }

    private void initializePlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (needsToSeek){
                    mp.seekTo((int) mainVM.getLastSongPlayedDuration());
                    needsToSeek = false;
                    return;
                }
                mp.start();
                mainVM.setLastSongPlayedDuration(0);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                playNext();
                Log.i("OnCompletion", "OnPlaybackCompleted Listener");
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("onError", String.valueOf(what));
                mainVM.setLastSongPlayedDuration(0);
                mainVM.setCurrentAppState(AppState.APP_STATE.NOT_PLAYING);
                return false;
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                mp.start();
                mainVM.setLastSongPlayedDuration(0);
            }
        });
    }

    public void play(int position, long initialDuration){
        this.currentSongPosition = position;
        mainVM.setCurrentViewState(AppState.CURRENT_VIEW_STATE.SONG_PLAYING_LAYOUT);
        mainVM.setCurrentAppState(AppState.APP_STATE.PLAYING);
        playSong(initialDuration);
    }

    private void playSong(long initialDuration){
        needsToSeek = (initialDuration != 0);
        mediaPlayer.reset();

        SongModel currentSong = songs.get(currentSongPosition);

        mainVM.setLastSongPositionPlayed(currentSongPosition);
        mainVM.setLastSongPlayed(currentSong);

        long _id = currentSong._id;

        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, _id);

        try {
            Log.d("SOURCE", "Setting data source");
            mediaPlayer.setDataSource(MainApplication.get(this), songUri);
        } catch (IOException e) {
            Log.e("SOURCE", e.getLocalizedMessage());
            e.printStackTrace();
        }
        onSongPlayedListener.onSongPlayed();
        mediaPlayer.prepareAsync();
    }

    public void playNext(){
        Log.d("NEXT", "Playing next song");

        currentSongPosition++;
        if (currentSongPosition >= songs.size()){
            currentSongPosition = 0;
            playSong(0);
            return;
        }

        playSong(0);
    }

    public void playPrevious(){
        Log.d("PREVIOUS", "Playing previous song");
        currentSongPosition--;
        if (currentSongPosition <= 0){
            currentSongPosition = songs.size() - 1;
            playSong(0);
            return;
        }
        playSong(0);
    }

    public void pausePlaying(){
        mediaPlayer.pause();
        mainVM.setLastSongPlayedDuration(mediaPlayer.getCurrentPosition());
        mainVM.setCurrentAppState(AppState.APP_STATE.NOT_PLAYING);
        onSongPausedListener.onSongPaused();
    }

    public void setServiceVariables(MainVM.OnSongPlayedListener onSongPlayedListener,
                                    MainVM.OnSongStoppedListener onSongStoppedListener,
                                    MainVM.OnSongPausedListener onSongPausedListener){
        this.onSongPausedListener = onSongPausedListener;
        this.onSongPlayedListener = onSongPlayedListener;
        this.onSongStoppedListener = onSongStoppedListener;

//        Log.i("Size of song passed", String.valueOf(model.size()));
    }

    public class MainBinder extends Binder {
        public MainService getService(){
            return MainService.this;
        }
    }
}
