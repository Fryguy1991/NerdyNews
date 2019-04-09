package com.chrisfry.nerdnews.utils

import android.util.Log

/**
 * Utils class for logging to LogCat or console (if testing)
 */
class LogUtils {
    companion object {
        // Flag indicating if we are in testing mode
        var isTesting = false

        /**
         * Log tag and message to debug channel (or console if testing)
         *
         * @param tag: Tag string for the log message
         * @param message: Message to log
         */
        fun debug(tag: String, message: String) {
            if (isTesting) {
                println("D/$tag: $message")
            } else {
                Log.d(tag, message)
            }
        }

        /**
         * Log tag and message to error channel (or console if testing)
         *
         * @param tag: Tag string for the log message
         * @param message: Message to log
         */
        fun error(tag: String, message: String) {
            if (isTesting) {
                println("E/$tag: $message")
            } else {
                Log.e(tag, message)
            }
        }

        /**
         * Log tag and message to warning channel (or console if testing)
         *
         * @param tag: Tag string for the log message
         * @param message: Message to log
         */
        fun warning(tag: String, message: String) {
            if (isTesting) {
                println("W/$tag: $message")
            } else {
                Log.w(tag, message)
            }
        }

        /**
         * Log tag and message to information channel (or console if testing)
         *
         * @param tag: Tag string for the log message
         * @param message: Message to log
         */
        fun information(tag: String, message: String) {
            if (isTesting) {
                println("I/$tag: $message")
            } else {
                Log.i(tag, message)
            }
        }

        /**
         * Log tag and message to verbose channel (or console if testing)
         *
         * @param tag: Tag string for the log message
         * @param message: Message to log
         */
        fun verbose(tag: String, message: String) {
            if (isTesting) {
                println("V/$tag: $message")
            } else {
                Log.v(tag, message)
            }
        }

        /**
         * Log tag and message to wtf channel (or console if testing)
         *
         * @param tag: Tag string for the log message
         * @param message: Message to log
         */
        fun wtf(tag: String, message: String) {
            if (isTesting) {
                println("WTF/$tag: $message")
            } else {
                Log.wtf(tag, message)
            }
        }
    }
}