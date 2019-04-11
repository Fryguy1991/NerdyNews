package com.chrisfry.nerdnews.tests

import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.business.enums.NewsApiCountrys
import com.chrisfry.nerdnews.business.enums.NewsApiLanguages
import org.junit.Assert
import org.junit.Test

class NewsEnumsTest: BaseTest() {

    @Test
    fun testCountryEnum() {
        // Build hash of supported country codes and their expected enum value
        val supportedCountryHash = HashMap<String, NewsApiCountrys>()
        for (country: NewsApiCountrys in NewsApiCountrys.values()) {
            supportedCountryHash[country.code] = country
        }

        // Ensure our supported country codes return the correct enum
        for(code: String in supportedCountryHash.keys) {
            Assert.assertEquals(supportedCountryHash[code], NewsApiCountrys.getCountry(code) )
        }

        // Ensure our supported country codes return the correct enum when in uppercase
        for(code: String in supportedCountryHash.keys) {
            Assert.assertEquals(supportedCountryHash[code], NewsApiCountrys.getCountry(code.toUpperCase()))
        }

        // Example of some invalid country codes
        val listOfInvalidCodes = listOf(
            AppConstants.EMPTY_STRING,
            "1",
            "12",
            "USA",
            "United State",
            "SomeSuperLongNonExistentCountryCode"
        )

        // All of the above codes should return US enum
        for (invalidCode: String in listOfInvalidCodes) {
            Assert.assertEquals(NewsApiCountrys.US, NewsApiCountrys.getCountry(invalidCode))
        }
    }

    @Test
    fun testLanguageEnum() {
        // Build hash of supported languages and their expected enum value
        val supportedLanguageHash = HashMap<String, NewsApiLanguages>()
        for (language: NewsApiLanguages in NewsApiLanguages.values()) {
            supportedLanguageHash[language.code] = language
        }

        // Ensure our supported language codes return the correct enum
        for(code: String in supportedLanguageHash.keys) {
            Assert.assertEquals(supportedLanguageHash[code], NewsApiLanguages.getLanguage(code) )
        }

        // Ensure our supported language codes return the correct enum when in uppercase
        for(code: String in supportedLanguageHash.keys) {
            Assert.assertEquals(supportedLanguageHash[code], NewsApiLanguages.getLanguage(code.toUpperCase()))
        }

        // Example of some invalid codes
        val listOfInvalidCodes = listOf(
            AppConstants.EMPTY_STRING,
            "1",
            "12",
            "ENG",
            "English",
            "SomeSuperLongNonExistentLanguageCode"
        )

        // All of the above codes should return EN enum
        for (invalidCode: String in listOfInvalidCodes) {
            Assert.assertEquals(NewsApiLanguages.EN, NewsApiLanguages.getLanguage(invalidCode))
        }
    }
}