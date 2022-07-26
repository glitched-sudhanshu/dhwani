package com.plcoding.spotifycloneyt.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.SwipeSongAdapter
import com.plcoding.spotifycloneyt.data.entities.Song
import com.plcoding.spotifycloneyt.exoplayer.isPlaying
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
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObservers()
        vpSong.adapter = swipeSongAdapter

        vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
           //this function is called everytime we swipe in our view pager or do any other changes
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying == true){
                    mainViewModel.playOrToggle(swipeSongAdapter.songs[position])
                } else{
                    curPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        ivPlayPause.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggle(it, true)
            }
        }

    }

    //this will change the view pager to the current song
    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex != -1) {
            //current item being displayed in vpSong
            vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    //to observe the change of sing
    private fun subscribeToObservers() {
        //when new mediaItem is introduced
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                //loading image to view pager image view
                                glide.load((curPlayingSong ?: songs[0]).imageUrl)
                                    .into(ivCurSongImage)
                            }
                            //return out of the observe block by default if curPlaying song is null
                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
                        }
                    }
                    ERROR -> Unit
                    LOADING -> Unit
                }

            }
        }
        //when curPlaying song changes
        mainViewModel.curPlayingSong.observe(this) {
            if (it == null) return@observe
            curPlayingSong = it.toSong()
            glide.load(curPlayingSong?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
        }

        //this observer will be called everytime when we play, pause or change the music etc
        mainViewModel.playbackState.observe(this) {
            playbackState = it
            ivPlayPause.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause
                else R.drawable.ic_play
            )
        }

        mainViewModel.isConnected.observe(this){
            //we have an event object of type resource of type boolean and if you remember we used this event object to just don't show the snackbar, that we want to show here in case of an error case, twice. so if we would rotate the screen usually these observers here would fire up again but since we wrapped this around this event object it won't happen so because the event class will just handle that so that this event can be emitted only once
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> Snackbar.make(
                        rootLayout, result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this){
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    ERROR -> Snackbar.make(
                        rootLayout, result.message ?: "An unknown error occurred",
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Unit
                }
            }
        }
    }
}