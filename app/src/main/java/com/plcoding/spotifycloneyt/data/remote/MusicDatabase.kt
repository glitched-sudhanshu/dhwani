package com.plcoding.spotifycloneyt.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.plcoding.spotifycloneyt.data.entities.Song
import com.plcoding.spotifyclone.other.Constans.SONG_COLLECTION
import kotlinx.coroutines.tasks.await

class MusicDatabase {
    private val firestore = FirebaseFirestore.getInstance()
    private val songCollection = firestore.collection(SONG_COLLECTION)

    //suspend keyword to make the function able to use coroutines. Since we are fetching data from cloud we will need to use coroutines
    suspend fun getAllSongs(): List<Song>
    {
        return try {
            //"get" will get all the documents in that collection
            //"await" makes get function a suspend function so that it will be executed in a coroutine
            //"await" will give us an object of type any.
            //that is why we use toObjects function after that
            songCollection.get().await().toObjects(Song::class.java)
        } catch (e: Exception)
        {
            emptyList()
        }
    }
}