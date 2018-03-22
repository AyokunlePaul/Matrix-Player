package com.dev.eipeks.lecteur.screen.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.dev.eipeks.lecteur.MainApplication;
import com.dev.eipeks.lecteur.R;
import com.dev.eipeks.lecteur.core_package.dagger.component.MainComponent;
import com.dev.eipeks.lecteur.core_package.view.CoreActivity;
import com.dev.eipeks.lecteur.databinding.MainLayoutBinding;

/**
 * Created by eipeks on 3/19/18.
 */

public class Main extends CoreActivity {

    MainLayoutBinding binding;
    MainComponent component;

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
//        binding.setVariable()
    }

    private void makeToast(String message){
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public class MainClickListener{
        public void onRecyclerViewItemClicked(View view){
            makeToast("Item Clicked");
        }
    }

}
