package com.example.dhwani.other

//With this class we only want to trigger specific events in our app a single time
open class Event<out T>(private val data: T) {

    //initially it is false that means it will trigger the event but once it is triggered we will set it equal to true. Then afterwards it just won't emit this event again but will emit only null
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if(hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            data
        }
    }

    fun peekContent() = data
}