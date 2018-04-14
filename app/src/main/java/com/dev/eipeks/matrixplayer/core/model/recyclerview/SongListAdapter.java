package com.dev.eipeks.matrixplayer.core.model.recyclerview;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dev.eipeks.matrixplayer.R;
import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.dev.eipeks.matrixplayer.databinding.SongItemsBinding;
import com.dev.eipeks.matrixplayer.global.Utils;


import java.util.List;

import javax.inject.Inject;

/**
 * Created by eipeks on 3/27/18.
 */

public class SongListAdapter extends RecyclerView.Adapter<SongListAdapter.SongViewHolder>{

    private List<SongModel> songs;
    private LayoutInflater inflater;

    private Context context;

    private PlaySongListener listener;

    public SongListAdapter(List<SongModel> songs, Context context){
        Log.i("Matrix Player: Adapter", String.valueOf(songs.size()));
        this.songs = songs;
        this.context = context;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (inflater == null){
            inflater = LayoutInflater.from(context);
        }

        SongItemsBinding binding = DataBindingUtil.inflate(inflater, R.layout.song_items, parent, false);
        return new SongViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongViewHolder holder, int position) {
        SongModel model = songs.get(position);

        Bitmap bitmap = Utils.getSongIconPath(model.songPath);

        if (bitmap != null){
            Log.d("IMAGE SIZE", Integer.toString(bitmap.getByteCount()));
            Log.d("IMAGE SIZE BITMAP", Integer.toString(bitmap.getRowBytes() * bitmap.getHeight()));
            holder.getBinding().songImage.setImageURI(Utils.getImageUri(context, bitmap));
        }
        holder.getBinding().songItemRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null){
                    listener.onPlaySongClicked(holder.getAdapterPosition());
                }
            }
        });
        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public List<SongModel> getSongs(){
        return this.songs;
    }

    public void setPlayClickedListener(PlaySongListener listener){
        this.listener = listener;
    }

    public interface PlaySongListener{
        void onPlaySongClicked(int position);
    }

    public class SongViewHolder extends RecyclerView.ViewHolder {

        private SongItemsBinding binding;

        public SongViewHolder(SongItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(SongModel model){
            binding.setSong(model);
        }

        public SongItemsBinding getBinding(){
            return binding;
        }
    }

}
