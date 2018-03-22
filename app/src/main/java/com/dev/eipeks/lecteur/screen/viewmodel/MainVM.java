package com.dev.eipeks.lecteur.screen.viewmodel;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.dev.eipeks.lecteur.core_package.model.SongModel;
import com.dev.eipeks.lecteur.core_package.view.CoreVM;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainVM extends CoreVM {

    public List<SongModel> getSongs(Context context){
        List<SongModel> songs = new ArrayList<>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
                MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);

        if (cursor != null && cursor.moveToFirst()){
            do {
                long _id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                String songName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                String songArtist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));

                SongModel model = new SongModel(songName, songArtist, _id);
                songs.add(model);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return songs;
    }

}
