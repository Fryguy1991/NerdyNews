package com.chrisfry.nerdnews.business.eventhandling.events

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.eventhandling.BaseEvent

/**
 * Event fired after more articles are added to the model of given type
 *
 * @param articleType: Type of article that has received more articles
 */
class MoreArticleEvent(val articleType: ArticleDisplayType): BaseEvent()