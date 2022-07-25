package com.plcoding.spotifycloneyt.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.plcoding.spotifycloneyt.R
import com.plcoding.spotifycloneyt.adapters.SwipeSongAdapter
import com.plcoding.spotifycloneyt.exoplayer.MusicServiceConnection
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

//to tell dagger this is the module of our app
@Module
//In parenthesis we have to specify a component where we want to install this module. This decides the scope of the module.
//ApplicationComponent just means that all the dependencies inside this app module will reside as long as app is running
@InstallIn(ApplicationComponent::class)
object AppModule {

    //singleton means that there will be only a single instance of this function all over the application

//@ApplicationContext because now dagger knows from where it needs to take the context
    //For every parameter there should be a provider of the parameter.
    @Singleton
    @Provides
    fun provideGlideInstance(
        @ApplicationContext context: Context
    ) = Glide.with(context).setDefaultRequestOptions(
        RequestOptions()
                //when image is not fully loaded
            .placeholder(R.drawable.ic_image)
                //if any error happens
            .error(R.drawable.ic_image)
                //to make sure that image is cached with glide
            .diskCacheStrategy(DiskCacheStrategy.DATA)
    )


    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context
    ) = MusicServiceConnection(context)

    @Singleton
    @Provides
    fun provideSwipeSongAdapter() = SwipeSongAdapter()

}