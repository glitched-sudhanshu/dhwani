package com.plcoding.spotifycloneyt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.bumptech.glide.RequestManager
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.SwipeSongAdapter
import com.plcoding.spotifycloneyt.data.entities.Song
import com.plcoding.spotifycloneyt.exoplayer.toSong
import com.plcoding.spotifycloneyt.other.Status
import com.plcoding.spotifycloneyt.other.Status.*
import com.plcoding.spotifycloneyt.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

//If we inject something in android components then we need to annotate that component(activity in this case) with
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //here we want to bind it to the lifecycle of the activity
    private val mainViewModel : MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObservers()
        vpSong.adapter = swipeSongAdapter

    }

    //this will change the view pager to the current song
    private fun switchViewPagerToCurrentSong(song : Song){
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex != -1){
            //current item being displayed in vpSong
            vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    //to observe the change of sing
    private fun subscribeToObservers(){
        //when new mediaItem is introduced
        mainViewModel.mediaItems.observe(this){
            it?.let{ result->
                when(result.status){
                    SUCCESS ->{
                        result.data?.let{ songs->
                            swipeSongAdapter.songs = songs
                            if(songs.isNotEmpty()){
                                //loading image to view pager image view
                                glide.load((curPlayingSong ?: songs[0]).imageUrl).into(ivCurSongImage)
                            }
                            //return out of the observe block by default if curPlaying song is null
                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
                        }
                    }
                    ERROR ->Unit
                    LOADING -> Unit
                }

            }
        }
        //when curPlaying song changes
        mainViewModel.curPlayingSong.observe(this){
            if(it == null)return@observe
            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }
    }
}