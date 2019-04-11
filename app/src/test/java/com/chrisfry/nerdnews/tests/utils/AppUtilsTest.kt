package com.chrisfry.nerdnews.tests.utils

import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.tests.BaseTest
import com.chrisfry.nerdnews.utils.AppUtils
import org.junit.Assert
import org.junit.Test

class AppUtilsTest: BaseTest() {
    @Test
    fun testBuildCommaSeparatedString() {
        val testList1 = listOf<String>()
        val testList2 = listOf("one", "two","three","four","five","six","seven","eight","nine","ten")
        val testList3 = listOf("thisisonestring")
        val testList4 = listOf("ign.com", "polygon.com", "kotaku.com", "gamespot.com", "gamesradar.com",
            "gamerant.com","nintendolife.com", "pushsquare.com")

        val expectedOutput1 = AppConstants.EMPTY_STRING
        val expectedOutput2 = "one,two,three,four,five,six,seven,eight,nine,ten"
        val expectedOutput3 = "thisisonestring"
        val expectedOutput4 = "ign.com,polygon.com,kotaku.com,gamespot.com,gamesradar.com,gamerant.com,nintendolife.com,pushsquare.com"

        val outputString1 = AppUtils.buildCommaSeparatedString(testList1)
        val outputString2 = AppUtils.buildCommaSeparatedString(testList2)
        val outputString3 = AppUtils.buildCommaSeparatedString(testList3)
        val outputString4 = AppUtils.buildCommaSeparatedString(testList4)

        Assert.assertTrue(expectedOutput1 == outputString1)
        Assert.assertTrue(expectedOutput2 == outputString2)
        Assert.assertTrue(expectedOutput3 == outputString3)
        Assert.assertTrue(expectedOutput4 == outputString4)
    }
}