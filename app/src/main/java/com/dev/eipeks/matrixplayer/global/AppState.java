package com.dev.eipeks.matrixplayer.global;

/**
 * Created by eipeks on 3/31/18.
 */

public interface AppState {
    enum APP_STATE{
        PLAYING("Playing"), NOT_PLAYING("Not Playing");

        private String appStateName;

        APP_STATE(String stateName){
            this.appStateName = stateName;
        }

        @Override
        public String toString() {
            return appStateName;
        }
    }

    enum PLAYER_STATE{
        SHUFFLE_ON("Shuffle On"), REPEAT_ON("Repeat On");

        private String playerStateName;

        PLAYER_STATE(String playerStateName) {
            this.playerStateName = playerStateName;
        }

        @Override
        public String toString() {
            return playerStateName;
        }
    }

    enum CURRENT_VIEW_STATE{
        SONG_PLAYING_LAYOUT("Song Playing Layout"), MAIN_DISPLAY_LAYOUT("Main Display Layout");

        private String playerStateName;

        CURRENT_VIEW_STATE(String playerStateName) {
            this.playerStateName = playerStateName;
        }

        @Override
        public String toString() {
            return playerStateName;
        }
    }
}
