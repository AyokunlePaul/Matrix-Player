package com.dev.eipeks.matrixplayer.core.model;

/**
 * Created by eipeks on 3/19/18.
 */

public class SongModel {

    public SongModel(String songName, String songArtist, long _id, String songPath) {
        this.songName = songName;
        this.songArtist = songArtist;
        this._id = _id;
        this.songPath = songPath;
    }

    public String songName;
    public String songArtist;
    public long _id;
    public String songPath;

}
