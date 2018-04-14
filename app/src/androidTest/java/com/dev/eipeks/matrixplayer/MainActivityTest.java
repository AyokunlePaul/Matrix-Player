package com.dev.eipeks.matrixplayer;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.dev.eipeks.matrixplayer.screen.activity.MainActivity;

import org.junit.Rule;
import org.junit.runner.RunWith;

/**
 * Created by eipeks on 4/13/18.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    ActivityTestRule<MainActivity> main = new ActivityTestRule<>(MainActivity.class);



}
