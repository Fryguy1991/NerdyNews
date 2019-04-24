package com.chrisfry.nerdnews.business.events

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType

/**
 * Event fired after more articles are added to the model of given type
 *
 * @param articleType: Type of article that has received more articles
 */
class MoreArticleEvent(val articleType: ArticleDisplayType)