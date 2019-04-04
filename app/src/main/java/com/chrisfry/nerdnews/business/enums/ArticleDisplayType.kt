package com.chrisfry.nerdnews.business.enums

import com.chrisfry.nerdnews.R

enum class ArticleDisplayType(val stringResourceId: Int) {
    TECH(R.string.tab_tech),
    SCIENCE(R.string.tab_science),
    GAMING(R.string.tab_gaming)
}