package com.dev.eipeks.matrixplayer.core.model.listview;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.dev.eipeks.matrixplayer.R;
import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.dev.eipeks.matrixplayer.global.Utils;

import java.util.List;

/**
 * Created by eipeks on 4/14/18.
 */

public class SongListAdapter extends ArrayAdapter<SongModel> {

    private Context context;
    private List<SongModel> songs;

    public SongListAdapter(@NonNull Context context, List<SongModel> songs) {
        super(context, 0, songs);
        this.context = context;
        this.songs = songs;
    }

    @Nullable
    @Override
    public SongModel getItem(int position) {
        return songs.get(position);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (convertView == null){
            convertView = inflater.inflate(R.layout.song_items, parent, false);
        }

        SongModel model = songs.get(position);

        TextView songName = convertView.findViewById(R.id.song_name);
        TextView songArtist = convertView.findViewById(R.id.song_artist);

        songName.setText(model.songName);
        songArtist.setText(model.songArtist);

        return convertView;
    }

}
