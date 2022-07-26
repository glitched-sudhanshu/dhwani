package com.plcoding.spotifycloneyt.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.spotifycloneyt.exoplayer.MusicService
import com.plcoding.spotifycloneyt.exoplayer.MusicServiceConnection
import com.plcoding.spotifycloneyt.exoplayer.currentPlaybackPosition
import com.plcoding.spotifycloneyt.other.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//what is specific to this view model, is the current position in our player because we will have a seekbar in which we just observe on the current player position we get from our music service.
class SongViewModel @ViewModelInject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel(){
    private val playbackState = musicServiceConnection.playbackState

    //amount of milliseconds the current song is long
    private val _curSongDuration = MutableLiveData<Long>()
    val curSongDuration : LiveData<Long> = _curSongDuration

    //at which millisecond the player is currently playing
    private val _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition : LiveData<Long> = _curPlayerPosition

    //here we will run a coroutine that will be bound to this song view model's life cycle and that coroutine will be continuously update the values of this player position and also this current song duration so that we can immediately get the values in fragment later on

    init {
        updateCurrentPlayerPosition()
    }


    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            //this coroutine will be cancelled once this view model is cleared

            //this could have been implemented in the main view model but then we keep this coroutine running in the mainViewModel even though we don't even need that
            while(true){
                val pos = playbackState.value?.currentPlaybackPosition
                if(curPlayerPosition.value != pos){
                    _curPlayerPosition.postValue(pos)
                    _curSongDuration.postValue(MusicService.currSongDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }


}