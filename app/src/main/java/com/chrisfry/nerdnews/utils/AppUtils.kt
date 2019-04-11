package com.chrisfry.nerdnews.utils

import com.chrisfry.nerdnews.AppConstants

/**
 * General utils class not handling display
 */
class AppUtils {
    companion object {
        private val TAG = AppUtils::class.java.simpleName

        /**
         * Build a comma separated string consisting of provided strings
         * Ex: List = "one", "two", "three" returns "one,two,three"
         *
         * @param stringList: List of strings that we want to be contained in the comma separated string
         * @return : A comma separated string with all provided strings
         */
        fun buildCommaSeparatedString(stringList: List<String>): String {
            var commaSeparatedString = AppConstants.EMPTY_STRING

            for (listItem: String in stringList) {
                when (commaSeparatedString.isEmpty()) {
                    true -> commaSeparatedString = listItem
                    false -> commaSeparatedString = "$commaSeparatedString,$listItem"
                }
            }
            return commaSeparatedString
        }
    }
}