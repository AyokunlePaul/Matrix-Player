package com.dev.eipeks.matrixplayer.screen.viewmodel;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.widget.Toast;

import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.dev.eipeks.matrixplayer.core.store.OfflineStore;
import com.dev.eipeks.matrixplayer.core.view.CoreVM;
import com.dev.eipeks.matrixplayer.global.AppState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.observers.DisposableObserver;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainVM extends CoreVM {

    private OfflineStore offlineStore;

    private AppState.APP_STATE appState;
    private AppState.CURRENT_VIEW_STATE currentViewState;

    private SongModel model;

    private List<SongModel> songList = new ArrayList<>();

    private int lastSongPosition;

    private long lastSongPlayedDuration;

    @Inject
    public MainVM(OfflineStore offlineStore){
        this.offlineStore = offlineStore;
    }

    public Single<List<SongModel>> queryLocalSongs(final Context context){
        return generateObservable(new Callable<List<SongModel>>() {
            @Override
            public List<SongModel> call() throws Exception {

                ContentResolver resolver = context.getContentResolver();
                Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                        MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);

                if (cursor != null && cursor.moveToFirst()){
                    do {
                        long _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        String songName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                        String songArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        String songPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        long songDuration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));

                        SongModel model = new SongModel(songName, songArtist, _id, songPath, songDuration);
                        songList.add(model);
                    } while (cursor.moveToNext());

                    cursor.close();
                }
                Collections.sort(songList);
                return songList;
            }
        });
    }

    public List<SongModel> getSongs(){
        return this.songList;
    }

    private static <T> Single<T> generateObservable(final Callable<T> func){
        return Single.create(new SingleOnSubscribe<T>() {
            @Override
            public void subscribe(SingleEmitter<T> emitter) throws Exception {
                try {
                    emitter.onSuccess(func.call());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void setCurrentAppState(AppState.APP_STATE appState){
        offlineStore.setCurrentAppState(appState);
    }

    public AppState.APP_STATE getAppState(){
        getCachedLastState();
        return appState;
    }

    public void setLastSongPlayed(SongModel model){
        offlineStore.cacheCurrentSong(model);
    }

    public SongModel getLastSongPlayed(){
        getCachedLastSong();
        return model;
    }

    public void setLastSongPositionPlayed(int position){
        offlineStore.setLastSongPlayedPosition(position);
    }

    public int getLastSongPositionPlayed(){
        getLastSongPosition();
        return lastSongPosition;
    }

    public void setCurrentViewState(AppState.CURRENT_VIEW_STATE currentViewState){
        offlineStore.setCurrentViewState(currentViewState);
    }

    public AppState.CURRENT_VIEW_STATE getCurrentViewState(){
        getCachedCurrentViewState();
        return currentViewState;
    }

    public void setLastSongPlayedDuration(long duration){
        offlineStore.setLastSongDuration(duration);
    }

    public long getLastSongPlayedDuration(){
        getLastSongDurationPlayed();
        return lastSongPlayedDuration;
    }

    private void getCachedLastState(){
        appState = offlineStore.getAppState();
    }

    private void getCachedLastSong(){
        model = offlineStore.getLastSongPlayed();
    }

    private void getLastSongPosition(){
        lastSongPosition = offlineStore.getLastSongPlayedPosition();
    }

    private void getCachedCurrentViewState(){
        currentViewState = offlineStore.getCurrentViewState();
    }

    private void getLastSongDurationPlayed(){
        lastSongPlayedDuration = offlineStore.getLastSongDuration();
    }

    public interface OnSongPlayedListener{
        void onSongPlayed();
    }

    public interface OnSongStoppedListener{
        void onSongStopped();
    }

    public interface OnSongPausedListener{
        void onSongPaused();
    }
}
