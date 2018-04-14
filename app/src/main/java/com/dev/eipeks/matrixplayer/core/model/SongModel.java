package com.dev.eipeks.matrixplayer.core.model;

import android.support.annotation.NonNull;

import java.util.Comparator;

/**
 * Created by eipeks on 3/19/18.
 */

public class SongModel implements Comparable<SongModel>{

    public SongModel(String songName, String songArtist, long _id, String songPath, long duration) {
        this.songName = songName;
        this.songArtist = songArtist;
        this._id = _id;
        this.songPath = songPath;
        this.duration = duration;
    }

    public String songName;
    public String songArtist;
    public long _id;
    public String songPath;
    public long duration;

    @Override
    public int compareTo(@NonNull SongModel o) {
        return this.songName.compareToIgnoreCase(o.songName);
    }
}
