package com.plcoding.spotifycloneyt.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.plcoding.spotifycloneyt.data.remote.MusicDatabase
import com.plcoding.spotifycloneyt.exoplayer.State.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.withContext
import javax.inject.Inject

//make sure we get all our songs from firebase and covert its format
class FirebaseMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
)
{
    var songs = emptyList<MediaMetadataCompat>()

    //will download songs from firestore
    //dispatchers will move to current thread to IO thread which is more suitable for network etc operations
    suspend fun fetchMediaData() = withContext(Dispatchers.IO){
        state = STATE_INITIALIZING
        val allSongs = musicDatabase.getAllSongs()

        //converting format of songs
        songs = allSongs.map {
            song -> MediaMetadataCompat.Builder()
            .putString(METADATA_KEY_ARTIST, song.subtitle)
            .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
            .putString(METADATA_KEY_TITLE, song.title)
            .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
            .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.subtitle)
            .putString(METADATA_KEY_DISPLAY_ICON_URI, song.imageUrl)
            .putString(METADATA_KEY_ALBUM_ART_URI, song.imageUrl)
            .putString(METADATA_KEY_MEDIA_URI, song.songUrl)
            .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.subtitle)
            .build()
        }
        state = STATE_INITIALIZED
        //when ever we are changing these states it is automatically calling the setter method we implemented below
    }

    //so media source in exoplayer is just a single song basically. To make a playlist, so we will have to implement a so-called concatenating mediaSource. It will be a list of music source

    //method to convert a song to a mediaSource
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory) : ConcatenatingMediaSource
    {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }

        return concatenatingMediaSource
    }

    //function for each media item, such as clicking a song will open other songs, that is a browsable application. For that we need a list of media items in our music service
    fun asMediaItems() = songs.map { song->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()

        //the flag is to it a playable item. Since it a media browser it could be anything. for eg An album to browse songs
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }

    //when we download our data from firebase, it takes some amount of time. Since we use coroutines. We need some sort of mechanism to let us know the downloads are finished.
    //In our app we need immediate actions. So with the help of this "onReadyListeners" we can schedule tasks when download is finished
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()


    private var state: State = STATE_CREATED
    set(value)
    {
        if(value == STATE_CREATED || value == STATE_ERROR)
        {
            //in both of these states the work is completed and nothing will occur. So, now we will call all of our onReadyListeners. But we have to do it in a thread safe way, so that we can change the state in multiple states at once

            //now only a single thread will access these onReadyListener
            synchronized(onReadyListeners){
                //FIELD is the value of the state
                field = value
                //loop over all these lambda functions
                onReadyListeners.forEach{
                    //yaha pe humne ye kiya h ki: hume malum h agar iss block me aaya h to ya to initialized hogya h ya phir error hai. To humne yaha check kiya <state == STATE_INITIALIZED> agar initialize hua hoga to <true> nahi hoga to iska mtlb error tha, to <false>. So we use this in other method that whether it was a success or not.
                    listener->listener(state == STATE_INITIALIZED)
                }
            }

        } else
        {
            field = value
        }
    }

    fun whenReady(action: (Boolean)->Unit) : Boolean {
        if(state == STATE_CREATED || state == STATE_INITIALIZING){
            //music source is not ready. So add the action to onReadyListeners to complete it later on
            onReadyListeners += action
            return false
        } else{
            action(state == STATE_INITIALIZED)
            return true
        }
    }
}

//refer this in case of any doubt : https://youtu.be/-rqYHUUpuqE

//TODO: What is enum class?
enum class State
{
    //here we will define various states in which music source can be in
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR,
}