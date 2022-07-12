package com.plcoding.spotifycloneyt.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.plcoding.spotifycloneyt.data.remote.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    //this is to allow FirebaseMusicSource class to use music database
    @ServiceScoped
    @Provides
    fun provideMusicDatabase() = MusicDatabase()

    //ServiceScoped is similar to "@Singleton", it means that this function will have a single instance in our same services
    @ServiceScoped
    @Provides
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @ServiceScoped
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes
    ) = SimpleExoPlayer.Builder(context).build().apply {
        setAudioAttributes(audioAttributes, true)
        //if user plugs in earphones, music will pause
        setHandleAudioBecomingNoisy(true)
    }

    @ServiceScoped
    @Provides
    fun provideDataSourceFactory(
        @ApplicationContext context: Context
    //second parameters requires a user agent which is basically just a name with which player can see who is actually connected to it
    ) = DefaultDataSourceFactory(context, Util.getUserAgent(context, "Spotify App"))
}