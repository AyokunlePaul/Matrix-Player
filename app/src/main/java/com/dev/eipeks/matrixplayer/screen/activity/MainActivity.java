package com.dev.eipeks.matrixplayer.screen.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dev.eipeks.matrixplayer.MainApplication;
import com.dev.eipeks.matrixplayer.R;
import com.dev.eipeks.matrixplayer.core.dagger.component.MainComponent;
import com.dev.eipeks.matrixplayer.core.managers.PermissionsManager;
import com.dev.eipeks.matrixplayer.core.managers.PopUpManager;
import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.dev.eipeks.matrixplayer.core.model.recyclerview.SongListAdapter;
import com.dev.eipeks.matrixplayer.core.store.OfflineStore;
import com.dev.eipeks.matrixplayer.core.view.CoreActivity;
import com.dev.eipeks.matrixplayer.databinding.ControllersBinding;
import com.dev.eipeks.matrixplayer.databinding.MainLayoutBinding;
import com.dev.eipeks.matrixplayer.databinding.SnackbarLayoutBinding;
import com.dev.eipeks.matrixplayer.databinding.SongPlayingLayoutBinding;
import com.dev.eipeks.matrixplayer.databinding.ToolbarMainBinding;
import com.dev.eipeks.matrixplayer.databinding.ToolbarSongPlayingBinding;
import com.dev.eipeks.matrixplayer.global.AppState;

import com.dev.eipeks.matrixplayer.global.Constants;

import com.dev.eipeks.matrixplayer.global.Utils;
import com.dev.eipeks.matrixplayer.screen.viewmodel.MainVM;
import com.dev.eipeks.matrixplayer.service.MainService;
import com.xw.repo.BubbleSeekBar;

import java.io.File;
import java.util.Locale;
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
    private ToolbarSongPlayingBinding toolbarSongPlayingBinding;


    private MainComponent component;

    private MainService mainService;

    private MainVM.OnSongPlayedListener onSongPlayedListener;
    private MainVM.OnSongStoppedListener onSongStoppedListener;
    private MainVM.OnSongPausedListener onSongPausedListener;
    private MainVM.ShuffleSelectedListener shuffleSelectedListener;

    private Intent serviceIntent;

    private Snackbar snackbar;
    private SnackbarLayoutBinding snackbarBinding;

    private PopUpManager popUpManager;
    private PermissionsManager permissionsManager;
    private LinearLayoutManager layoutManager;

    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0){
                snackbar.dismiss();
            } else if (dy < 0){
                if (mainVM.getCurrentViewState() != null){
                    snackbar.show();
                }
            }
        }
    };

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
    };

    @Inject
    MainVM mainVM;

    SongListAdapter adapter;

    @Inject
    OfflineStore store;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("Lifecycle", "onCreate");
        super.onCreate(savedInstanceState);

        /*
         * Get MainComponent
         */
        component = MainApplication.get(this).getComponent();
        component.inject(this);

        //Shuffle the shuffledList
        if (mainVM.getShuffleState()){
            mainVM.startShuffle();
        }

        /*
         * Initialize binding
         */
        initBinding();

        /*
         * Initialize snackbar
         */
        initSnackbar();

        /*
         *  Set correct view
         */
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
         */
        adapter = new SongListAdapter(mainVM.getSongs(), MainActivity.this);
        adapter.setPlayClickedListener(MainActivity.this);

        /*
         *  Initialize Layout Manager
         */
        layoutManager = new LinearLayoutManager(this);

        /*
         * Check if there are no songs on the device
         */
        if (mainVM.getSongs().size() == 0){
            binding.songList.setVisibility(View.GONE);
            binding.noSongFoundLayout.setVisibility(View.VISIBLE);
        }

        /*
         * Bind adapter to recycler view
         */
        binding.songList.setLayoutManager(layoutManager);
        binding.songList.setNestedScrollingEnabled(false);
        binding.songList.setAdapter(adapter);
        binding.songList.setHasFixedSize(true);
        binding.songList.setItemViewCacheSize(mainVM.getSongs().size() * 2);
        binding.songList.setDrawingCacheEnabled(true);
        binding.songList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        binding.songList.addOnScrollListener(scrollListener);
    }

    @Override
    public void onBackPressed() {
        if (mainVM.getCurrentViewState().toString().equals(AppState.CURRENT_VIEW_STATE.SONG_PLAYING_LAYOUT.toString())){
            mainVM.setCurrentViewState(AppState.CURRENT_VIEW_STATE.MAIN_DISPLAY_LAYOUT);
            snackbarBinding.setSong(mainVM.getLastSongPlayed());
            snackbarBinding.getRoot().setVisibility(View.VISIBLE);
            setCorrectLayout();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        Log.d("Lifecycle", "onStart");
        super.onStart();

        initAppEventListeners();

        serviceIntent = new Intent(MainActivity.this, MainService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        startService(serviceIntent);
    }

    @Override
    protected void onResume() {
        Log.d("Lifecycle", "onResume");
        super.onResume();
        if (MainApplication.shouldPlaySongFromIntent){
            String id = getIntent().getStringExtra(Constants.CONSTANT_SONG_FROM_INTENT);
            mainVM.getSongInfoFromIntent(getContentResolver(), Uri.parse(id));
            return;
        }

        MainVM.activityIsVisible = true;
        if (mainService != null){
            mainService.dismissNotification();
        }
        if (mainVM.getLastSongPlayed() != null){
            currentProgress = (mainVM.getLastSongPlayedDuration() * 100) / mainVM.getLastSongPlayed().duration;
        } else {
            currentProgress = 0;
        }
        setCorrectLayout();
    }

    @Override
    protected void onPause() {
        Log.d("Lifecycle", "onPause");
        if (MainVM.mediaPlayerIsCurrentlyPlaying){
            mainService.showNotification();
        }
        MainVM.activityIsVisible = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d("Lifecycle", "onDestroy");
        stopService(serviceIntent);
        serviceConnection = null;
        serviceIntent = null;
        super.onDestroy();
    }

    @Override
    public void onPlaySongClicked(SongModel model) {
        if (progressObservable != null || observer != null){
            if (!observer.isDisposed()){
                stopProgressCount();
            }
        }
        currentProgress = 0;
        mainVM.setLastSongPlayedDuration(currentProgress);
        mainService.play(model, 0, AppState.CURRENT_VIEW_STATE.SONG_PLAYING_LAYOUT);
    }

    private void initSnackbar(){
        snackbar = Snackbar.make(binding.getRoot(), "", Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.removeAllViews();
        snackbarLayout.setPadding(0, 0, 0, 0);
        snackbarBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.snackbar_layout, null, false);
        snackbarBinding.setSong(mainVM.getLastSongPlayed());
        snackbarBinding.setControllersListener(new ControllersClickListener());

        snackbarLayout.addView(snackbarBinding.getRoot(), 0);
        if (mainVM.getCurrentViewState() != null){
            if (mainVM.getCurrentViewState().toString().equals(AppState.CURRENT_VIEW_STATE.MAIN_DISPLAY_LAYOUT.toString())){
                snackbarBinding.getRoot().setVisibility(View.VISIBLE);
            }
        }
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

        songPlayingLayoutBinding.progressBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                Log.d("onProgressChanged", "Int progress" + Integer.toString(progress));
                Log.d("onProgressChanged", "Float progress" + Float.toString(progressFloat));
                Log.d("onProgressChanged", "From User" + Boolean.toString(fromUser));
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                Log.d("getProgressOnActionUp", "Int progress" + Integer.toString(progress));
                Log.d("getProgressOnActionUp", "Float progress" + Float.toString(progressFloat));
                long seekToValue = (progress * mainVM.getLastSongPlayed().duration) / 100;
                mainService.seekTo((int)seekToValue);
            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                Log.d("getProgressOnFinally", "Int progress" + Integer.toString(progress));
                Log.d("getProgressOnFinally", "Float progress" + Float.toString(progressFloat));
                Log.d("getProgressOnFinally", "From User" + Boolean.toString(fromUser));
            }
        });
    }

    private void initAppEventListeners(){
        onSongPlayedListener = new MainVM.OnSongPlayedListener() {
            @Override
            public void onSongPlayed() {
                controllersBinding.playPauseButton.setImageResource(R.drawable.ic_pause);
                snackbarBinding.playPauseButton.setImageResource(R.drawable.ic_pause);
                snackbarBinding.setSong(mainVM.getLastSongPlayed());
//                songPlayingLayoutBinding.backgroundImage.setImageBitmap(Utils.getSongBitmap(mainVM.getLastSongPlayed().songPath) != null ?
//                        Utils.getSongBitmap(mainVM.getLastSongPlayed().songPath): BitmapFactory.decodeResource(getResources(), R.drawable.song_playing_background));
                if (mainVM.getCurrentViewState().toString().equals(AppState.CURRENT_VIEW_STATE.MAIN_DISPLAY_LAYOUT.toString())){
                    snackbarBinding.getRoot().setVisibility(View.VISIBLE);
                }
                setCorrectLayout();
                createObservableAndObserver(mainVM.getLastSongPlayedDuration(), mainVM.getLastSongPlayed().duration);
                startProgressCount();
            }
        };

        onSongStoppedListener = new MainVM.OnSongStoppedListener() {
            @Override
            public void onSongStopped() {
                controllersBinding.playPauseButton.setImageResource(R.drawable.ic_play);
                snackbarBinding.playPauseButton.setImageResource(R.drawable.ic_play);
                stopProgressCount();

            }
        };

        onSongPausedListener = new MainVM.OnSongPausedListener() {
            @Override
            public void onSongPaused() {
                controllersBinding.playPauseButton.setImageResource(R.drawable.ic_play);
                snackbarBinding.playPauseButton.setImageResource(R.drawable.ic_play);
                pauseProgressCount();
            }
        };

        shuffleSelectedListener = new MainVM.ShuffleSelectedListener() {
            @Override
            public void onShuffleSelected(boolean shuffleState) {
                if (shuffleState){
                    mainVM.startShuffle();
                    binding.songPlayingLink.controllers.shuffleSongsButton.setSelected(true);
                } else {
                    binding.songPlayingLink.controllers.shuffleSongsButton.setSelected(false);
                }
            }
        };
    }

    /*
    *  Method to ensure to current layout matches correct state
    * */
    private void restoreState(){
        //I'm initializing the observer and observable to prevent crash in onCreate
        createObservableAndObserver(0,0);
        setCorrectLayout();
    }

    private void cacheAllStates(){

    }

    private void setCorrectLayout(){
        AppState.CURRENT_VIEW_STATE state = mainVM.getCurrentViewState();

        if (state == null){
            return;
        }

        if (mainVM.getShuffleState()){
            binding.songPlayingLink.controllers.shuffleSongsButton.setSelected(true);
        }

        if (state.toString().equals(AppState.CURRENT_VIEW_STATE.SONG_PLAYING_LAYOUT.toString())){
            binding.songListLayout.setVisibility(View.GONE);
            snackbarBinding.getRoot().setVisibility(View.GONE);
            binding.songPlayingLink.getRoot().setVisibility(View.VISIBLE);
            binding.songPlayingLink.progressBar.setProgress(currentProgress);

            long elapsedTimeInSeconds = mainVM.getLastSongPlayedDuration() / 1000;
            long totalTimeInSeconds = mainVM.getLastSongPlayed().duration / 1000;

            long hoursElapsed = elapsedTimeInSeconds / 3600;
            long minutesElapsed = elapsedTimeInSeconds / 60;
            long secondsElapsed = elapsedTimeInSeconds % 60;

            String elapsedTimeFormat = hoursElapsed > 0 ?
                    String.format(Locale.ENGLISH, "%02d:%02d%02d", hoursElapsed, minutesElapsed, secondsElapsed) :
                    String.format(Locale.ENGLISH, "%02d:%02d", minutesElapsed, secondsElapsed);

            binding.songPlayingLink.elapsedTime.setText(elapsedTimeFormat);

            long hoursInLastPlayedSong = totalTimeInSeconds / 3600;
            long minutesInLastPlayedSong = totalTimeInSeconds / 60;
            long secondsInLastPlayedSong = totalTimeInSeconds % 60;

            String totalTime = hoursInLastPlayedSong > 0 ?
                    String.format(Locale.ENGLISH, "%02d:%02d%02d", hoursInLastPlayedSong, minutesInLastPlayedSong, secondsInLastPlayedSong) :
                    String.format(Locale.ENGLISH, "%02d:%02d", minutesInLastPlayedSong, secondsInLastPlayedSong);

            binding.songPlayingLink.songDuration.setText(totalTime);

            if (mainVM.getAppState().toString().equals(AppState.APP_STATE.PLAYING.toString())){
                controllersBinding.playPauseButton.setImageResource(R.drawable.ic_pause);
                snackbarBinding.playPauseButton.setImageResource(R.drawable.ic_pause);
            } else {
                controllersBinding.playPauseButton.setImageResource(R.drawable.ic_play);
                snackbarBinding.playPauseButton.setImageResource(R.drawable.ic_play);
            }
            binding.songPlayingLink.songInfoLink.setSong(mainVM.getLastSongPlayed());
        } else if (state.toString().equals(AppState.CURRENT_VIEW_STATE.MAIN_DISPLAY_LAYOUT.toString())){
            binding.songListLayout.setVisibility(View.VISIBLE);
            snackbarBinding.getRoot().setVisibility(View.VISIBLE);
            binding.songPlayingLink.getRoot().setVisibility(View.GONE);
        }

    }

    private void createObservableAndObserver(long startValue, long duration){
        Log.d("Current song duration", Long.toString(duration));

        final long totalEmission = (duration / 1000);

        long hoursInSong = totalEmission / 3600;
        long minutesInPassed = totalEmission / 60;
        long secondsInPassed = totalEmission % 60;

        String totalTime = hoursInSong > 0 ?
                String.format(Locale.ENGLISH, "%02d:%02d%02d", hoursInSong, minutesInPassed, secondsInPassed) :
                String.format(Locale.ENGLISH, "%02d:%02d", minutesInPassed, secondsInPassed);
        binding.songPlayingLink.songDuration.setText(totalTime);

        if (observer != null && progressObservable != null){
            stopProgressCount();
        }

        progressObservable = Observable.intervalRange((startValue / 1000), totalEmission,
                0, 1000, TimeUnit.MILLISECONDS).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        observer = new DisposableObserver<Long>() {
            @Override
            public void onNext(Long aLong) {
                currentProgress = (aLong * 100) / totalEmission;

                Log.d("Long emitted", Long.toString(currentProgress));

                long hoursPresentInSong = totalEmission / 3600;

                long hoursPassed = aLong / 3600;
                long minutesPassed = aLong / 60;
                long secondsPassed = aLong % 60;

                String elapsedTimeFormat = hoursPresentInSong > 0 ?
                        String.format(Locale.ENGLISH, "%02d:%02d%02d", hoursPassed, minutesPassed, secondsPassed) :
                        String.format(Locale.ENGLISH, "%02d:%02d", minutesPassed, secondsPassed);

                binding.songPlayingLink.elapsedTime.setText(elapsedTimeFormat);
                binding.songPlayingLink.progressBar.setProgress(currentProgress);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                currentProgress = 0;
                binding.songPlayingLink.progressBar.setProgress(currentProgress);
            }
        };
    }

    private void startProgressCount(){
        progressObservable.subscribe(observer);
    }

    private void stopProgressCount(){
        currentProgress = 0;
        binding.songPlayingLink.progressBar.setProgress(currentProgress);
        observer.dispose();
        observer = null;
        progressObservable = null;
    }

    private void pauseProgressCount(){
        observer.dispose();
        binding.songPlayingLink.progressBar.setProgress(currentProgress);
        progressObservable = null;
        observer = null;
    }

    public class ControllersClickListener{
        public void onFavoriteIconClicked(View view){
            Toast.makeText(MainActivity.this, "Favorite option will be out in subsequent release.", Toast.LENGTH_SHORT).show();
        }

        public void onPlayPreviousIconClicked(View view){
            if (observer != null && progressObservable != null){
                stopProgressCount();
            }
            mainService.playPrevious();
        }

        public void onPlayPauseIconClicked(View view){
            if (mainVM.getAppState().toString().equals(AppState.APP_STATE.PLAYING.toString())){
                Log.d("STATE", "Song was playing");
                mainVM.setCurrentAppState(AppState.APP_STATE.NOT_PLAYING);
                mainService.pausePlaying();
            } else {
                Log.d("STATE", "Song was not playing");
                mainVM.setCurrentAppState(AppState.APP_STATE.PLAYING);
                mainService.play(mainVM.getLastSongPlayed(), mainVM.getLastSongPlayedDuration(), null);
            }
        }

        public void onPlayNextIconClicked(View view){
            if (observer != null && progressObservable != null){
                stopProgressCount();
            }
            mainService.playNext();
        }

        public void onShuffleIconClicked(View view){
            if (mainVM.getShuffleState()){
                shuffleSelectedListener.onShuffleSelected(false);
                mainVM.setShuffleState(false);
            } else {
                shuffleSelectedListener.onShuffleSelected(true);
                mainVM.setShuffleState(true);
            }
        }

        public void onMainLayoutClicked(View view){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainVM.setCurrentViewState(AppState.CURRENT_VIEW_STATE.SONG_PLAYING_LAYOUT);

                    if (mainVM.getAppState().toString().equals(AppState.APP_STATE.PLAYING.toString())){
                        stopProgressCount();
                        createObservableAndObserver(mainVM.getLastSongPlayedDuration(), mainVM.getLastSongPlayed().duration);
                        startProgressCount();
                    } else {
                        currentProgress = (mainVM.getLastSongPlayedDuration() * 100) / mainVM.getLastSongPlayed().duration;
                        binding.songPlayingLink.progressBar.setProgress(currentProgress);
                    }
                    setCorrectLayout();
                }
            }, 500);
        }
    }

    public class ToolbarClickListener{
        public void onBackPressed(View view){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mainVM.setCurrentViewState(AppState.CURRENT_VIEW_STATE.MAIN_DISPLAY_LAYOUT);
                    setCorrectLayout();
                }
            }, 500);
        }
    }

}
