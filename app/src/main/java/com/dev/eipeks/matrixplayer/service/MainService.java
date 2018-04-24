package com.dev.eipeks.matrixplayer.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.dev.eipeks.matrixplayer.MainApplication;
import com.dev.eipeks.matrixplayer.R;
import com.dev.eipeks.matrixplayer.core.dagger.component.MainComponent;
import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.dev.eipeks.matrixplayer.global.AppState;
import com.dev.eipeks.matrixplayer.global.Constants;
import com.dev.eipeks.matrixplayer.global.Utils;
import com.dev.eipeks.matrixplayer.screen.activity.MainActivity;
import com.dev.eipeks.matrixplayer.screen.viewmodel.MainVM;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.dev.eipeks.matrixplayer.global.Constants.NOTIFICATION_CHANNEL_ID;
import static com.dev.eipeks.matrixplayer.global.Constants.NOTIFICATION_ID;


/**
 * Created by eipeks on 3/19/18.
 */

public class MainService extends Service {

    private MainComponent component;

    private int currentSongPosition = 0;

    private List<SongModel> songs = new ArrayList<>();

    private MediaPlayer mediaPlayer;

    private MainVM.OnSongPlayedListener onSongPlayedListener;
    private MainVM.OnSongPausedListener onSongPausedListener;
    private MainVM.OnSongStoppedListener onSongStoppedListener;

    private boolean needsToSeek = false;
    private int seekToValue = 0;

    private Handler handler;
    private Runnable runnable;

    private MediaSessionCompat session;
    private NotificationManager manager;
    private NotificationCompat.Builder builder;

    private PendingIntent nextPendingIntent;
    private static PendingIntent pausePlayPendingIntent;
    private PendingIntent previousPendingIntent;

    private static MainService service;
    private static MainVM staticMainVM;
    private static ArrayList<NotificationCompat.Action> actions = new ArrayList<>();

    @Inject
    MainVM mainVM;

    @Override
    public void onCreate() {

        component = MainApplication.get(this).getComponent();
        component.inject(this);

        session = new MediaSessionCompat(this, "Main Service");
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        MainVM.pausePlayIcon = R.drawable.ic_pause;
        MainVM.isOnGoing = false;

        initializePlayer();

        initializePendingIntents();

        startTaskRepetition();

        Log.i("Song List size", String.valueOf(songs.size()));

        if (service == null){
            staticMainVM = mainVM;
            service = this;
        }

        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("Service", "Activity bounded");
        songs = mainVM.getSongs();
        if (service == null){
            staticMainVM = mainVM;
            service = this;
        }
        dismissNotification();
        return new MainBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mainVM.setCurrentAppState(AppState.APP_STATE.NOT_PLAYING);
        mainVM.setLastSongPlayedDuration(mediaPlayer.getCurrentPosition());
        mediaPlayer.stop();
        mediaPlayer.release();

        stopTaskRepetition();

        return true;
    }

    private void initializePendingIntents(){
        initializePlayPreviousIntent();
        initializePlayNextIntent();
        initializePlayPauseIntent();
    }

    private void initializePlayPreviousIntent(){
        Intent playPrevious = new Intent(this, PlayPreviousActionReceiver.class);
        playPrevious.putExtra("TYPE", 2);
        previousPendingIntent = PendingIntent.getBroadcast(this, Constants.PAUSE_PLAY_REQUEST_CODE,
                playPrevious, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void initializePlayNextIntent(){
        Intent playNext = new Intent(this, PlayNextActionReceiver.class);
        playNext.putExtra("TYPE", 1);
        nextPendingIntent = PendingIntent.getBroadcast(this, Constants.PAUSE_PLAY_REQUEST_CODE,
                playNext, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void initializePlayPauseIntent(){
        Intent pausePlayIntent = new Intent(this, PlayPauseActionReceiver.class);
        pausePlayIntent.putExtra("TYPE", 0);
        pausePlayPendingIntent = PendingIntent.getBroadcast(this, Constants.PAUSE_PLAY_REQUEST_CODE,
                pausePlayIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void initializePlayer(){
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (needsToSeek){
                    mp.seekTo(seekToValue);
                    needsToSeek = false;
                    return;
                }
                mp.start();
                mainVM.setCurrentAppState(AppState.APP_STATE.PLAYING);
                if (!MainVM.activityIsVisible){
                    showNotification();
                }
                onSongPlayedListener.onSongPlayed();
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.reset();
                playNext();
                Log.i("OnCompletion", "OnPlaybackCompleted Listener");
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.e("onError", String.valueOf(what));
                mainVM.setLastSongPlayedDuration(0);
                mainVM.setCurrentAppState(AppState.APP_STATE.NOT_PLAYING);
                MainVM.mediaPlayerIsCurrentlyPlaying = false;
                return false;
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                mp.start();
                if (!MainVM.activityIsVisible){
                    showNotification();
                }
                onSongPlayedListener.onSongPlayed();
            }
        });
    }

    public void play(SongModel model, long initialDuration, AppState.CURRENT_VIEW_STATE currentViewState){
        if (currentViewState != null){
            mainVM.setCurrentViewState(currentViewState);
        }
        currentSongPosition = songs.indexOf(model);
        playSong(initialDuration, model);
    }

    private void playSong(long initialDuration, SongModel model){
        needsToSeek = (initialDuration != 0);
        if (needsToSeek){
            seekToValue = (int)initialDuration;
        }
        mediaPlayer.reset();

        mainVM.setLastSongPositionPlayed(currentSongPosition);
        mainVM.setLastSongPlayed(model);

        long _id = model._id;

        Uri songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, _id);

        try {
            Log.d("SOURCE", "Setting data source");
            mediaPlayer.setDataSource(MainApplication.get(this), songUri);
        } catch (IOException e) {
            Log.e("SOURCE", e.getLocalizedMessage());
            e.printStackTrace();
        }
        mainVM.setCurrentAppState(AppState.APP_STATE.PLAYING);
        mainVM.setLastSongPlayedDuration(initialDuration);
        MainVM.mediaPlayerIsCurrentlyPlaying = true;
        MainVM.pausePlayIcon = R.drawable.ic_pause;
        MainVM.isOnGoing = true;
        mediaPlayer.prepareAsync();
    }

    public void playNext(){
        Log.d("NEXT", "Playing next song");

        if (mainVM.getShuffleState()){
            playNextFromShuffledList();
            return;
        }

        playNextFromUnshuffledList();
    }

    private void playNextFromUnshuffledList(){
        currentSongPosition = songs.indexOf(mainVM.getLastSongPlayed());
        currentSongPosition++;

        if (currentSongPosition >= songs.size()){
            currentSongPosition = 0;
        }

        playSong(0, songs.get(currentSongPosition));
    }

    private void playNextFromShuffledList(){
        currentSongPosition++;

        if (currentSongPosition >= songs.size()){
            currentSongPosition = 0;
        }

        SongModel model;

        model = mainVM.getShuffledList().get(currentSongPosition);
        playSong(0, model);

    }

    public void playPrevious(){
        if (mediaPlayer.getCurrentPosition() > 4000){
            playSong(0, mainVM.getLastSongPlayed());
            return;
        }
        if (mainVM.getShuffleState()){
            playPreviousFromShuffledList();
            return;
        }

        playPreviousFromUnshuffledList();
    }

    private void playPreviousFromUnshuffledList(){
        Log.d("PREVIOUS", "Playing previous song");

        currentSongPosition = songs.indexOf(mainVM.getLastSongPlayed());
        currentSongPosition--;

        if (currentSongPosition < 0){
            currentSongPosition = songs.size() - 1;
        }

        playSong(0, songs.get(currentSongPosition));
    }

    private void playPreviousFromShuffledList(){
        SongModel model;

        if (currentSongPosition < 0){
            currentSongPosition = songs.size() - 1;
        }

        model = mainVM.getShuffledList().get(currentSongPosition);
        playSong(0, model);
    }

    public void seekTo(int seekToValue){
        mediaPlayer.seekTo(seekToValue);
        mainVM.setLastSongPlayedDuration(seekToValue);
    }

    public void pausePlaying(){
        Log.d("ACTIVITY IS VISIBLE", Boolean.toString(MainVM.activityIsVisible));
        if (!MainVM.activityIsVisible){
            showNotification();
        }
        MainVM.mediaPlayerIsCurrentlyPlaying = false;
        MainVM.isOnGoing = false;
        mediaPlayer.pause();
        mainVM.setLastSongPlayedDuration(mediaPlayer.getCurrentPosition());
        mainVM.setCurrentAppState(AppState.APP_STATE.NOT_PLAYING);
        onSongPausedListener.onSongPaused();
    }

    public void setServiceVariables(MainVM.OnSongPlayedListener onSongPlayedListener,
                                    MainVM.OnSongStoppedListener onSongStoppedListener,
                                    MainVM.OnSongPausedListener onSongPausedListener){
        this.onSongPausedListener = onSongPausedListener;
        this.onSongPlayedListener = onSongPlayedListener;
        this.onSongStoppedListener = onSongStoppedListener;
    }

    public void showNotification(){
        manager.notify(0, createMediaNotification());
    }

    public void dismissNotification(){
        if (manager != null){
            manager.cancel(0);
        }
    }

    private Notification createMediaNotification(){
        builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        builder.setAutoCancel(true);
        builder.setContentTitle(mainVM.getLastSongPlayed().songArtist);
        builder.setContentText(mainVM.getLastSongPlayed().songName);
        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                .setMediaSession(session.getSessionToken())
                .setShowActionsInCompactView(0, 1, 2));
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setContentIntent(PendingIntent.getActivity(this, 2938,
                new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setLargeIcon(Utils.getSongBitmap(mainVM.getLastSongPlayed().songPath) != null ?
                Utils.getSongBitmap(mainVM.getLastSongPlayed().songPath) :
                BitmapFactory.decodeResource(getResources(), R.drawable.music_playing_default));
        builder.setOnlyAlertOnce(true);
        builder.setOngoing(MainVM.isOnGoing);
        builder.addAction(new NotificationCompat.Action(R.drawable.prev_button_drawable, "Play Previous", previousPendingIntent));
        builder.addAction(new NotificationCompat.Action(MainVM.pausePlayIcon, "Pause Play", pausePlayPendingIntent));
        builder.addAction(new NotificationCompat.Action(R.drawable.next_button_drawable, "Play Next", nextPendingIntent));

        actions = builder.mActions;

        Log.d("ACTIONS LIST", Integer.toString(actions.size()));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "MATRIX_PLAYER_CHANNEL", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            assert manager != null;
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            manager.createNotificationChannel(notificationChannel);
        }

        return builder.build();
    }

    private static MainService get(){
        return service;
    }

    private void startTaskRepetition(){
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null){
                    if (mediaPlayer.isPlaying()){
                        Log.d("Runnable", "Last played position is caching " + mediaPlayer.getCurrentPosition());
                        mainVM.setLastSongPlayedDuration(mediaPlayer.getCurrentPosition());
                    }
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void stopTaskRepetition(){
        handler.removeCallbacks(runnable);
    }

    public class MainBinder extends Binder {
        public MainService getService(){
            return MainService.this;
        }
    }

    public static class PlayPauseActionReceiver extends BroadcastReceiver{

        public PlayPauseActionReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            int intentName = intent.getIntExtra("TYPE", 0);

            Log.d("TYPE VALUE ========== ", Integer.toString(intentName));

            if (intentName == 0){
                if (MainService.get().mediaPlayer.isPlaying()){
                    MainVM.pausePlayIcon = R.drawable.ic_play;
                    MainService.get().pausePlaying();
                    return;
                }
                MainVM.pausePlayIcon = R.drawable.ic_pause;
                MainService.get().playSong(staticMainVM.getLastSongPlayedDuration(), staticMainVM.getLastSongPlayed());
            }
        }
    }

    public static class PlayNextActionReceiver extends BroadcastReceiver{

        public PlayNextActionReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            int intentName = intent.getIntExtra("TYPE", 0);

            Log.d("TYPE VALUE ========== ", Integer.toString(intentName));

            if (intentName == 1){
                MainService.get().playNext();
            }
        }
    }

    public static class PlayPreviousActionReceiver extends BroadcastReceiver{

        public PlayPreviousActionReceiver(){}

        @Override
        public void onReceive(Context context, Intent intent) {
            int intentName = intent.getIntExtra("TYPE", 0);

            Log.d("TYPE VALUE ========== ", Integer.toString(intentName));

            if (intentName == 2){
                MainService.get().playPrevious();
            }
        }
    }
}
