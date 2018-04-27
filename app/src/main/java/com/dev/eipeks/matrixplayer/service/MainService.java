package com.dev.eipeks.matrixplayer.service;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;


/**
 * Created by eipeks on 3/19/18.
 */

public class MainService extends Service {

    private MainComponent component;

    private int currentSongPosition = 0;

    private List<SongModel> songs = new ArrayList<>();

    private MediaPlayer mediaPlayer;

    private MainVM.OnSongPlayedListener onSongPlayedListener;
    private MainVM.OnSongPausedListener onSongPausedListener;
    private MainVM.OnSongStoppedListener onSongStoppedListener;

    private boolean needsToSeek = false;
    private int seekToValue = 0;

    private Handler handler;
    private Runnable runnable;

    @Inject
    MainVM mainVM;

    @Override
    public void onCreate() {

        component = MainApplication.get(this).getComponent();
        component.inject(this);

        initializePlayer();

        startTaskRepetition();

        Log.i("Song List size", String.valueOf(songs.size()));

        super.onCreate();

        if (MainApplication.shouldPlaySongFromIntent){
            play(MainVM.songFromIntent, 0, AppState.CURRENT_VIEW_STATE.SONG_PLAYING_LAYOUT);
        }
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

        stopTaskRepetition();

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
                    mp.seekTo(seekToValue);
                    needsToSeek = false;
                    return;
                }
                mp.start();
                mainVM.setCurrentAppState(AppState.APP_STATE.PLAYING);
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
                onSongPlayedListener.onSongPlayed();
            }
        });
    }

    public void play(SongModel model, long initialDuration, AppState.CURRENT_VIEW_STATE currentViewState){
        if (currentViewState != null){
            mainVM.setCurrentViewState(currentViewState);
        }
        currentSongPosition = songs.indexOf(model);
        mainVM.setCurrentAppState(AppState.APP_STATE.PLAYING);
        playSong(initialDuration, model);
    }

    private void playSong(long initialDuration, SongModel model){
        needsToSeek = (initialDuration != 0);
        if (needsToSeek){
            seekToValue = (int)initialDuration;
        }
        mediaPlayer.reset();

        mainVM.setLastSongPositionPlayed(currentSongPosition);
        mainVM.setLastSongPlayed(model);

        long _id = model._id;

        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, _id);

        try {
            Log.d("SOURCE", "Setting data source");
            mediaPlayer.setDataSource(MainApplication.get(this), songUri);
        } catch (IOException e) {
            Log.e("SOURCE", e.getLocalizedMessage());
            e.printStackTrace();
        }
        mainVM.setLastSongPlayedDuration(initialDuration);
        onSongPlayedListener.onSongPlayed();
        mediaPlayer.prepareAsync();
    }

    public void playNext(){
        Log.d("NEXT", "Playing next song");

        currentSongPosition++;

        SongModel model;

        if (currentSongPosition >= songs.size()){
            currentSongPosition = 0;
        }

        if (mainVM.getShuffleState()){
            Collections.shuffle(songs, new Random(System.currentTimeMillis()));
            model = songs.get(currentSongPosition);
            playSong(0, model);
            return;
        }

        Collections.sort(songs);

        playSong(0, songs.get(currentSongPosition));
    }

    public void playPrevious(){
        Log.d("PREVIOUS", "Playing previous song");
        currentSongPosition--;

        SongModel model;

        if (currentSongPosition < 0){
            currentSongPosition = songs.size() - 1;
        }

        if (mainVM.getShuffleState()){
            Collections.shuffle(songs, new Random(System.currentTimeMillis()));
            model = songs.get(currentSongPosition);
            playSong(0, model);
            return;
        }

        Collections.sort(songs);

        playSong(0, songs.get(currentSongPosition));
    }

    public void seekTo(int seekToValue){
        mediaPlayer.seekTo(seekToValue);
        mainVM.setLastSongPlayedDuration(seekToValue);
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
    }

    private void startTaskRepetition(){
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {

                if (mediaPlayer != null){
                    if (mediaPlayer.isPlaying()){
                        Log.d("Runnable", "Last played position is caching " + mediaPlayer.getCurrentPosition());
                        mainVM.setLastSongPlayedDuration(mediaPlayer.getCurrentPosition());
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void stopTaskRepetition(){
        handler.removeCallbacks(runnable);
    }

    public class MainBinder extends Binder {
        public MainService getService(){
            return MainService.this;
        }
    }
}
