package com.chrisfry.nerdnews.business.eventhandling.events

import com.chrisfry.nerdnews.business.enums.ArticleDisplayType
import com.chrisfry.nerdnews.business.eventhandling.BaseEvent

/**
 * Event for broadcasting when an article type refresh is complete
 *
 * @param articleDisplayType: Article type that has completed refreshing
 */
class RequestMoreArticleEvent(val articleDisplayType: ArticleDisplayType): BaseEvent()