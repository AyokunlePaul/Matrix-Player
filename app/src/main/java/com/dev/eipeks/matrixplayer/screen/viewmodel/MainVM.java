package com.dev.eipeks.matrixplayer.screen.viewmodel;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.dev.eipeks.matrixplayer.core.view.CoreVM;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainVM extends CoreVM {

//    public Observable<List<SongModel>> getSongs(Context context){
//
//
//        return Observable.fromArray(songs)
//                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
//    }

    public Observable<List<SongModel>> getSongs(final Context context){
        return generateObservable(new Callable<List<SongModel>>() {
            @Override
            public List<SongModel> call() throws Exception {
                List<SongModel> songs = new ArrayList<>();

                ContentResolver resolver = context.getContentResolver();
                Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                        MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);

                if (cursor != null && cursor.moveToFirst()){
                    do {
                        long _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        String songName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                        String songArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        String songPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                        SongModel model = new SongModel(songName, songArtist, _id, songPath);
                        songs.add(model);
                    } while (cursor.moveToNext());

                    cursor.close();
                }

                return songs;
            }
        });
    }

    private static <T> Observable<T> generateObservable(final Callable<T> func){
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> emitter) throws Exception {
                try {
                    emitter.onNext(func.call());
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

}
