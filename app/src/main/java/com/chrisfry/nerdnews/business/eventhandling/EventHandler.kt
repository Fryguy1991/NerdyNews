package com.chrisfry.nerdnews.business.eventhandling

import com.chrisfry.nerdnews.business.eventhandling.events.ArticleRefreshCompleteEvent
import com.chrisfry.nerdnews.business.eventhandling.events.MoreArticleEvent
import com.chrisfry.nerdnews.business.eventhandling.receivers.ArticleRefreshCompleteEventReceiver
import com.chrisfry.nerdnews.business.eventhandling.receivers.MoreArticleEventReceiver
import com.chrisfry.nerdnews.utils.LogUtils

/**
 * Static class used for sending events between presenters
 */
class EventHandler {
    companion object {
        private val TAG = EventHandler::class.java.simpleName

        // List of all event receivers
        private val eventReceivers = mutableListOf<BaseEventReceiver>()

        /**
         * Add a event receiver to handle events
         *
         * @param receiver: New receiver that will listen for events
         */
        fun addEventReceiver(receiver: BaseEventReceiver) {
            eventReceivers.add(receiver)
        }

        /**
         * Broadcasts a given event to it's associated receivers
         *
         * @param event: Event object to be sent to receivers
         */
        fun broadcast(event: BaseEvent) {
            LogUtils.debug(TAG, "Broadcasting Event: ${event::class.java.simpleName}")

            // Loop through event receivers checking if they need to receive the broadcasted event
            for (receiver: BaseEventReceiver in eventReceivers) {
                when (event) {
                    // ArticleRefreshCompleteEventReceivers should receive ArticleRefreshCompleteEvents
                    is ArticleRefreshCompleteEvent -> {
                        if (receiver is ArticleRefreshCompleteEventReceiver) {
                            receiver.onReceive(event)
                        }
                    }
                    // MoreArticleEventReceivers should receive MoreArticleEvents
                    is MoreArticleEvent -> {
                        if (receiver is MoreArticleEventReceiver) {
                            receiver.onReceive(event)
                        }
                    }
                }
            }
        }

        /**
         * Remove all event receivers from the event handler
         */
        fun clearAllReceivers() {
            eventReceivers.clear()
        }
    }
}