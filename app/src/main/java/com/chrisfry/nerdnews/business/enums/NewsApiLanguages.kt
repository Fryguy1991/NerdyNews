package com.chrisfry.nerdnews.business.enums

/**
 * Enum representing the available languages for the NewsAPI
 * (see https://newsapi.org/docs/endpoints/everything language request parameter)
 */
enum class NewsApiLanguages(val code: String) {
    AR("ar"),
    DE("de"),
    EN("en"),
    ES("es"),
    FR("fr"),
    HE("he"),
    IT("it"),
    NL("nl"),
    NO("no"),
    PT("pt"),
    RU("ru"),
    SE("se"),
    UD("ud"),
    ZH("zh");

    companion object {

        /**
         * Returns an available language code depending on requested code (default is English)
         *
         * @param code: Requested language code (needs to be 2 letter ISO-639-1 language code)
         */
        fun getLanguage(code: String): NewsApiLanguages {
            // Handle accidentally being sent uppercase language code
            val codeToCheck = code.toLowerCase()

            // Ensure requested code is not empty and is a 2 letter code
            if (codeToCheck.isNotEmpty() && codeToCheck.length == 2) {
                for (language: NewsApiLanguages in values()) {
                    if (language.code == codeToCheck) {
                        return language
                    }
                }
            }
            return EN
        }
    }
}