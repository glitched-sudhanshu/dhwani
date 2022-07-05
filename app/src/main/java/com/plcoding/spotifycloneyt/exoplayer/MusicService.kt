package com.plcoding.spotifycloneyt.exoplayer

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject

/**
 *MediaBrowserServiceCompat is to help us to implement such media service.
 * It is a BROWSER because you can think a music player as of a file manager, where there are files and more folders in a folder. Similarly, in a music player app we can navigate to albums, playlist etc so this MediaBrowserServiceCompat helps us to create such an environment
*/

private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory : DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    private val serviceJob = Job()
    //service scope will deal with the life time of the coroutines
    private val serviceScoped = CoroutineScope(Dispatchers.Main + serviceJob)

    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var mediaSessionConnector : MediaSessionConnector

    override fun onCreate() {
        super.onCreate()

        //to open our activity when we click on the notification
        //this is a normal intent which will lead us to the activity
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        //if we want to play media, we have something like mediaSession which contains important info about the data about the current media session which we can use later on to communicate with our service
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true;
        }

        //to get the data of the media session
        sessionToken = mediaSession.sessionToken

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlayer(exoPlayer)
    }

    override fun onDestroy() {
        super.onDestroy()
        //remove coroutines when job is done
        serviceScoped.cancel()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        //parentId is to get a list of songs, so could just call the root id which will just give the default songs, then we could have an id for a specific playlist that would just return the songs in the playlist
        //Thought we will have a single id in our app, but if we need to make a more browsable music player app we need to put more stuff into this onLoadChildren
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }
}