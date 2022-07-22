package com.plcoding.spotifycloneyt.exoplayer

import android.support.v4.media.session.PlaybackStateCompat

inline val PlaybackStateCompat.isPrepared
get() = state == PlaybackStateCompat.STATE_BUFFERING ||
        state == PlaybackStateCompat.STATE_PLAYING ||
        state == PlaybackStateCompat.STATE_PAUSED

inline val PlaybackStateCompat.isPlaying
    get() = state == PlaybackStateCompat.STATE_BUFFERING ||
            state == PlaybackStateCompat.STATE_PLAYING

inline val PlaybackStateCompat.isPlayEnabled
            //This statement will be true only if play is enabled
    get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L ||
                    //If play pause button is itself enabled
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L &&
                    //if we are in the paused state we can say that now we can play the song
                state == PlaybackStateCompat.STATE_PAUSED)