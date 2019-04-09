package com.chrisfry.nerdnews.business.eventhandling

import com.chrisfry.nerdnews.business.eventhandling.events.RefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.RefreshEvent
import com.chrisfry.nerdnews.business.eventhandling.receivers.RefreshCompleteEventReceiver
import com.chrisfry.nerdnews.business.eventhandling.receivers.RefreshEventReceiver
import com.chrisfry.nerdnews.utils.LogUtils

/**
 * Static class used for sending events between presenters
 */
class EventHandler {
    companion object {
        private val TAG = EventHandler::class.java.name

        // TODO: Consider refactoring to 1 list of presenters and checking by type
        private val refreshReceivers = mutableListOf<RefreshEventReceiver>()
        private val refreshCompleteReceivers = mutableListOf<RefreshCompleteEventReceiver>()

        /**
         * Adds a receiver looking for RefreshEvents
         */
        fun addRefreshReceiver(receiver: RefreshEventReceiver) {
            refreshReceivers.add(receiver)
        }

        /**
         * Adds a receiver looking for RefreshCompleteEvents
         */
        fun addRefreshCompleteReceiver(receiver: RefreshCompleteEventReceiver) {
            refreshCompleteReceivers.add(receiver)
        }

        /**
         * Broadcasts a given event to it's associated receivers
         *
         * @param event: Event object to be sent to receivers
         */
        fun broadcast(event: BaseEvent) {
            LogUtils.debug(TAG, "Broadcasting Event: ${event::class.java.simpleName}")

            // Determine who will be receiving event based on event type
            val receivers: List<BaseEventReceiver>
            when (event::class.java) {
                RefreshEvent::class.java -> {
                    receivers = refreshReceivers
                }
                RefreshCompleteEvent::class.java -> {
                    receivers = refreshCompleteReceivers
                }
                else -> {
                    LogUtils.error(TAG, "${event::class.java} event not handled here")
                    return
                }
            }

            // Notify receivers of event
            for (receiver: BaseEventReceiver in receivers) {
                receiver.onReceive(event)
            }
        }
    }
}