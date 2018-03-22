package com.dev.eipeks.lecteur.core_package.model;

/**
 * Created by eipeks on 3/19/18.
 */

public class SongModel {

    public SongModel(String songName, String songArtist, long _id) {
        this.songName = songName;
        this.songArtist = songArtist;
        this._id = _id;
    }

    public String songName;
    public String songArtist;
    public long _id;

}
