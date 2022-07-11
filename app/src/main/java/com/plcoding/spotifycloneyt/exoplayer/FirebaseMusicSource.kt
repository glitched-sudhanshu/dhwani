package com.plcoding.spotifycloneyt.exoplayer

//make sure we get all our songs from firebase and covert its format
class FirebaseMusicSource
{
    //when we download our data from firebase, it takes some amount of time. Since we use coroutines. We need some sort of mechanism to let us know the downloads are finished.
    //In our app we need immediate actions. So with the help of this "onReadyListeners" we can schedule tasks when download is finished
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()


    private var state: State = State.STATE_CREATED
    set(value)
    {
        if(value == State.STATE_CREATED)
    }
}

//TODO: What is enum class?
enum class State
{
    //here we will define various states in which music source can be in
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR,
}