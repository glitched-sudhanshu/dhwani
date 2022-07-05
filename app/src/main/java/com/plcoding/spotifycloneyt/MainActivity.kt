package com.plcoding.spotifycloneyt

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.RequestManager
import com.plcoding.spotifyclone.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

//If we inject something in android components then we need to annotate that component(activity in this case) with
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var glide: RequestManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}