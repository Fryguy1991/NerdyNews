package com.chrisfry.nerdnews.tests

import com.chrisfry.nerdnews.business.eventhandling.EventHandler
import com.chrisfry.nerdnews.utils.LogUtils
import org.junit.After
import org.junit.Before

open class BaseTest {
    @Before
    open fun setUp() {
        // Ensure we're using test log methods so presenters don't need Android implementation
        LogUtils.isTesting = true
    }

    @After
    open fun tearDown() {
        // Ensure our event handler removes all receiver references
        EventHandler.clearAllReceivers()
    }
}