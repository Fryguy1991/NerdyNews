package com.chrisfry.nerdnews.business.eventhandling

/**
 * Base event receiver class used for receiving events from EventHandler
 */
interface BaseEventReceiver {
    fun onReceive(event: BaseEvent)
}