package com.dev.eipeks.matrixplayer.screen.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.dev.eipeks.matrixplayer.MainApplication;
import com.dev.eipeks.matrixplayer.R;
import com.dev.eipeks.matrixplayer.core.dagger.component.MainComponent;
import com.dev.eipeks.matrixplayer.core.managers.PermissionsManager;
import com.dev.eipeks.matrixplayer.core.managers.PopUpManager;
import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.dev.eipeks.matrixplayer.core.model.recyclerview.SongLayoutManager;
import com.dev.eipeks.matrixplayer.core.model.recyclerview.SongListAdapter;
import com.dev.eipeks.matrixplayer.core.store.OfflineStore;
import com.dev.eipeks.matrixplayer.core.view.CoreActivity;
import com.dev.eipeks.matrixplayer.databinding.ControllersBinding;
import com.dev.eipeks.matrixplayer.databinding.ImagePlayingIconBinding;
import com.dev.eipeks.matrixplayer.databinding.MainLayoutBinding;
import com.dev.eipeks.matrixplayer.databinding.SongPlayingLayoutBinding;
import com.dev.eipeks.matrixplayer.databinding.ToolbarMainBinding;
import com.dev.eipeks.matrixplayer.databinding.ToolbarSongPlayingBinding;
import com.dev.eipeks.matrixplayer.global.AppState;
import com.dev.eipeks.matrixplayer.screen.viewmodel.MainVM;
import com.dev.eipeks.matrixplayer.service.MainService;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainActivity extends CoreActivity implements SongListAdapter.PlaySongListener {

    //Default layout binding
    private MainLayoutBinding binding;
    private ToolbarMainBinding toolbarMainBinding;
    private SongPlayingLayoutBinding songPlayingLayoutBinding;

    //Song playing binding
    private ControllersBinding controllersBinding;
    private ImagePlayingIconBinding imagePlayingBinding;
    private ToolbarSongPlayingBinding toolbarSongPlayingBinding;


    private MainComponent component;

    private MainService mainService;

    private MainVM.OnSongPlayedListener onSongPlayedListener;
    private MainVM.OnSongStoppedListener onSongStoppedListener;
    private MainVM.OnSongPausedListener onSongPausedListener;

    private Intent serviceIntent;

    private PopUpManager popUpManager;
    private PermissionsManager permissionsManager;

    private long currentProgress = 0;

    private DisposableObserver<Long> observer;
    private Observable<Long> progressObservable;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainService = ((MainService.MainBinder) service).getService();
            MainApplication.serviceBoundToActivity = true;
            mainService.setServiceVariables(onSongPlayedListener, onSongStoppedListener, onSongPausedListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            MainApplication.serviceBoundToActivity = false;
        }
    };;

    @Inject
    MainVM mainVM;

    SongListAdapter adapter;

    @Inject
    OfflineStore store;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        * Get MainComponent
        * */
        component = MainApplication.get(this).getComponent();
        component.inject(this);

        /*
        * Initialize binding
        * */
        initBinding();

        /*
        *  Set correct view
        * */
        if (!(mainVM.getCurrentViewState() == null)){
            restoreState();
        }

        /*
         *Initialize managers
         */
        popUpManager = new PopUpManager(this);
        permissionsManager = new PermissionsManager(this);

        /*
        * Set up Adapter
        * */
        adapter = new SongListAdapter(mainVM.getSongs(), MainActivity.this);
        adapter.setPlayClickedListener(MainActivity.this);

        /*
        * Bind adapter to recycler view
        * */
        binding.songList.setLayoutManager(new SongLayoutManager(this));
        binding.songList.setNestedScrollingEnabled(false);
        binding.songList.setAdapter(adapter);
        binding.songList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                recyclerView.smoothScrollToPosition(dy);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mainVM.getCurrentViewState().toString().equals(AppState.CURRENT_VIEW_STATE.SONG_PLAYING_LAYOUT.toString())){
            mainVM.setCurrentViewState(AppState.CURRENT_VIEW_STATE.MAIN_DISPLAY_LAYOUT);
            setCorrectLayout();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();

        initAppEventListeners();

        serviceIntent = new Intent(MainActivity.this, MainService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setCorrectLayout();
    }

    @Override
    protected void onDestroy() {
        stopService(serviceIntent);
        serviceConnection = null;
        serviceIntent = null;
        super.onDestroy();
    }

    @Override
    public void onPlaySongClicked(int position) {
        if (progressObservable != null || observer != null){
            if (!observer.isDisposed()){
                stopProgressCount();
            }
        }
        mainService.play(position, 0);
        mainVM.setCurrentAppState(AppState.APP_STATE.PLAYING);
        mainVM.setCurrentViewState(AppState.CURRENT_VIEW_STATE.SONG_PLAYING_LAYOUT);
    }

    private void initBinding(){
        //Initialize main layout bindings
        binding = DataBindingUtil.setContentView(this, R.layout.main_layout);
        toolbarMainBinding = binding.mainToolbarLink;
        songPlayingLayoutBinding = binding.songPlayingLink;

        //Initialize song playing layout bindings
        controllersBinding = songPlayingLayoutBinding.controllers;
        toolbarSongPlayingBinding = songPlayingLayoutBinding.songPlayingToolbar;

        //Set listeners
        controllersBinding.setClickListener(new ControllersClickListener());
        toolbarSongPlayingBinding.setToolbarClickListener(new ToolbarClickListener());
    }

    private void initAppEventListeners(){
        onSongPlayedListener = new MainVM.OnSongPlayedListener() {
            @Override
            public void onSongPlayed() {
                controllersBinding.playPauseButton.setImageResource(R.drawable.ic_pause);
                setCorrectLayout();
                createObservableAndObserver(mainVM.getLastSongPlayedDuration(), mainVM.getLastSongPlayed().duration);
                startProgressCount();
            }
        };

        onSongStoppedListener = new MainVM.OnSongStoppedListener() {
            @Override
            public void onSongStopped() {
                controllersBinding.playPauseButton.setImageResource(R.drawable.ic_play);
            }
        };

        onSongPausedListener = new MainVM.OnSongPausedListener() {
            @Override
            public void onSongPaused() {
                controllersBinding.playPauseButton.setImageResource(R.drawable.ic_play);
            }
        };
    }

    /*
    *  Method to ensure to current layout matches correct state
    * */
    private void restoreState(){
        setCorrectLayout();
    }

    private void setCorrectLayout(){
        AppState.CURRENT_VIEW_STATE state = mainVM.getCurrentViewState();

        if (state == null){
            return;
        }

        if (state.toString().equals(AppState.CURRENT_VIEW_STATE.SONG_PLAYING_LAYOUT.toString())){
            binding.songListLayout.setVisibility(View.GONE);
            binding.songPlayingLink.getRoot().setVisibility(View.VISIBLE);
            binding.songPlayingLink.progressBar.setProgress(mainVM.getLastSongPlayedDuration());

            if (mainVM.getAppState().toString().equals(AppState.APP_STATE.PLAYING.toString())){
                controllersBinding.playPauseButton.setImageResource(R.drawable.ic_pause);
            } else {
                controllersBinding.playPauseButton.setImageResource(R.drawable.ic_play);
            }
//            binding.songPlayingLink.songInfoLink.songImage.setVisibility(View.GONE);
            binding.songPlayingLink.songInfoLink.setSong(mainVM.getLastSongPlayed());
        } else if (state.toString().equals(AppState.CURRENT_VIEW_STATE.MAIN_DISPLAY_LAYOUT.toString())){
            binding.songListLayout.setVisibility(View.VISIBLE);
            binding.songPlayingLink.getRoot().setVisibility(View.GONE);
        }

    }

    private void createObservableAndObserver(long startValue, long duration){
        Log.d("Current song duration", Long.toString(duration));

        final long totalEmission = (duration / 1000);

        progressObservable = Observable.intervalRange((startValue / 1000), totalEmission,
                0, 1000, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        observer = new DisposableObserver<Long>() {
            @Override
            public void onNext(Long aLong) {
                currentProgress = (aLong * 100) / totalEmission;

                Log.d("Long emitted", Long.toString(currentProgress));

                binding.songPlayingLink.progressBar.setProgress(currentProgress);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                binding.songPlayingLink.progressBar.setProgress(0);
            }
        };
    }

    private void startProgressCount(){
        progressObservable.subscribe(observer);
    }

    private void stopProgressCount(){
        binding.songPlayingLink.progressBar.setProgress(0);
        observer.dispose();
        progressObservable = null;
    }

    private void pauseProgressCount(){
        observer.dispose();
        binding.songPlayingLink.progressBar.setProgress(currentProgress);
        progressObservable = null;
    }

    public class ControllersClickListener{
        public void onFavoriteIconClicked(View view){

        }

        public void onPlayPreviousIconClicked(View view){
            stopProgressCount();
            mainService.playPrevious();
        }

        public void onPlayPauseIconClicked(View view){
            if (mainVM.getAppState().toString().equals(AppState.APP_STATE.PLAYING.toString())){
                mainService.pausePlaying();
                pauseProgressCount();
            } else {
                mainService.play(mainVM.getLastSongPositionPlayed(), mainVM.getLastSongPlayedDuration());
            }
        }

        public void onPlayNextIconClicked(View view){
            stopProgressCount();
            mainService.playNext();
        }

        public void onShuffleIconClicked(View view){

        }
    }

    public class ToolbarClickListener{
        public void onBackPressed(View view){
            mainVM.setCurrentViewState(AppState.CURRENT_VIEW_STATE.MAIN_DISPLAY_LAYOUT);
            setCorrectLayout();
        }
    }
}
