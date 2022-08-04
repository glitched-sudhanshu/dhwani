package com.example.dhwani.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.example.dhwani.other.Constants.MEDIA_ROOT_ID
import com.example.dhwani.other.Constants.NETWORK_ERROR
import com.example.dhwani.exoplayer.callbacks.MusicPlaybackPreparer
import com.example.dhwani.exoplayer.callbacks.MusicPlayerEventListener
import com.example.dhwani.exoplayer.callbacks.MusicPlayerNotificationListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 *MediaBrowserServiceCompat is to help us to implement such media service.
 * It is a BROWSER because you can think a music player as of a file manager, where there are files and more folders in a folder. Similarly, in a music player app we can navigate to albums, playlist etc so this MediaBrowserServiceCompat helps us to create such an environment
 */

private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private lateinit var musicNotificationManager: MusicNotificationManager

    companion object {
        var currSongDuration = 0L
            private set
    }

    private val serviceJob = Job()

    //service scope will deal with the life time of the coroutines
    private val serviceScoped = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private var currPlayingSong: MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    private lateinit var musicPlayerEventListener: MusicPlayerEventListener

    override fun onCreate() {
        super.onCreate()

        //loading firebaseMediaSource to fetch data
        serviceScoped.launch {
            firebaseMusicSource.fetchMediaData()
        }

        //to open our activity when we click on the notification
        //this is a normal intent which will lead us to the activity
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        //if we want to play media, we have something like mediaSession which contains important info about the data about the current media session which we can use later on to communicate with our service
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        //to get the data of the media session
        sessionToken = mediaSession.sessionToken

        //notification manager of our music service
        musicNotificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicPlayerNotificationListener(this)
        ) {
            //here will be a lambda function which will be called everytime when a song switches
            //we will update the current duration of the song that is playing
            currSongDuration = exoPlayer.duration
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource) {
            currPlayingSong = it
            preparePlayer(
                firebaseMusicSource.songs,
                it,
                true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }

    //to pass description of the song to the notification
    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }


    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val currSongIndex = if (currPlayingSong == null) 0 else songs.indexOf(itemToPlay)

        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(currSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        //when intent is removed
        exoPlayer.stop()
    }


    override fun onDestroy() {
        super.onDestroy()
        //remove coroutines when job is done
        serviceScoped.cancel()

        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }


    //since it is a browsable app we need to pass the ID from where all the data were actually coming from (like parent id). In our case it Firebase
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        //if want to any verification of out user to access any data or to restrict any data that functionality would be handled here
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        //parentId is to get a list of songs, so could just call the root id which will just give the default songs, then we could have an id for a specific playlist that would just return the songs in the playlist
        //Thought we will have a single id in our app, but if we need to make a more browsable music player app we need to put more stuff into this onLoadChildren
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId) {
            MEDIA_ROOT_ID -> {
                //here onLoadChildren is called even before our music source is ready and that's again a case when we need a whenReady function

                val resultsSent = firebaseMusicSource.whenReady { isInitialized ->
                    if(isInitialized) {
                        result.sendResult(firebaseMusicSource.asMediaItems())
                        //when we call this we want to prepare our player. So, we need to check if our player is initialized or not. If we don't do this the player will automatically start playing the music when we oen the app
                        if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {
                            preparePlayer(firebaseMusicSource.songs, firebaseMusicSource.songs[0], false)
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession.sendSessionEvent(NETWORK_ERROR, null)
                        //if player is ready but not initialized
                        //if player is ready but not initialized
                        result.sendResult(null)
                    }
                }
                if(!resultsSent) {
                    result.detach()
                }
            }
        }
    }
}