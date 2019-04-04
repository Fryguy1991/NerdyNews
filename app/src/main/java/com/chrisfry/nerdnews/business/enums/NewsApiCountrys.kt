package com.chrisfry.nerdnews.business.enums

/**
 * Enum representing the available countries for the NewsAPI
 * (see https://newsapi.org/docs/endpoints/top-headlines country request parameter)
 */
enum class NewsApiCountrys(val code: String) {
    AE("ae"),
    AR("ar"),
    AT("at"),
    AU("au"),
    BE("be"),
    BG("bg"),
    BR("br"),
    CA("ca"),
    CH("ch"),
    CN("cn"),
    CO("co"),
    CU("cu"),
    CZ("cz"),
    DE("de"),
    EG("eg"),
    FR("fr"),
    GB("gb"),
    GR("gr"),
    HK("hk"),
    HU("hu"),
    ID("id"),
    IE("ie"),
    IL("il"),
    IN("in"),
    IT("it"),
    JP("jp"),
    KR("kr"),
    LT("lt"),
    LV("lv"),
    MA("ma"),
    MX("mx"),
    MY("my"),
    NG("ng"),
    NL("nl"),
    NO("no"),
    NZ("nz"),
    PH("ph"),
    PL("pl"),
    PT("pt"),
    RO("ro"),
    RS("rs"),
    RU("ru"),
    SA("sa"),
    SE("se"),
    SG("sg"),
    SI("si"),
    SK("sk"),
    TH("th"),
    TR("tr"),
    TW("tw"),
    UA("ua"),
    US("us"),
    VE("ve"),
    ZA("za");

    companion object {
        /**
         * Returns an available country depending on requested code (default is United States)
         *
         * @param code: Requested language code (needs to be 2 letter ISO 3166-1 country code)
         */
        fun getCountry(code: String): NewsApiCountrys {
            // Handle accidentally being sent uppercase country code
            val codeToCheck = code.toLowerCase()

            // Ensure requested code is not empty and is a 2 letter code
            if (codeToCheck.isNotEmpty() && codeToCheck.length == 2) {
                for (language: NewsApiCountrys in values()) {
                    if (language.code == codeToCheck) {
                        return language
                    }
                }
            }
            return US
        }
    }
}