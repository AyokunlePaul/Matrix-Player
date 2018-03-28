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
import android.util.Log;
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
import com.dev.eipeks.matrixplayer.databinding.MainLayoutBinding;
import com.dev.eipeks.matrixplayer.screen.viewmodel.MainVM;
import com.dev.eipeks.matrixplayer.service.MainService;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainActivity extends CoreActivity implements SongListAdapter.PlaySongListener {

    private MainLayoutBinding binding;
    private MainComponent component;

    private MainService mainService;

    private Intent serviceIntent;

    private PopUpManager popUpManager;
    private PermissionsManager permissionsManager;

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mainService = ((MainService.MainBinder) service).getService();
            MainApplication.serviceBoundToActivity = true;
            mainService.setSongs(adapter.getSongs());
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
        binding = DataBindingUtil.setContentView(this, R.layout.main_layout);

//        Log.i("Matrix Player: Activity", String.valueOf(adapter.getSongs().size()));

        /*
         *Initialize managers
         */
        popUpManager = new PopUpManager(this);
        permissionsManager = new PermissionsManager(this);

        /*
        * Bind adapter to recycler view
        * */
        binding.songList.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (serviceIntent == null){

            mainVM.getSongs(this)
                    .observeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<List<SongModel>>() {
                        @Override
                        public void onNext(List<SongModel> songModels) {
                            adapter = new SongListAdapter(songModels, MainActivity.this);
                            adapter.setPlayClickedListener(MainActivity.this);
                            binding.songList.setAdapter(adapter);

                            serviceIntent = new Intent(MainActivity.this, MainService.class);
                            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
                            startService(serviceIntent);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        stopService(serviceIntent);
        serviceIntent = null;
        super.onDestroy();
    }

    @Override
    public void onPlaySongClicked(int position) {
        Toast.makeText(this, String.valueOf(position) + " Clicked", Toast.LENGTH_SHORT).show();
        mainService.play(position);
    }

}
