package com.chrisfry.nerdnews.business.dagger.modules

import com.chrisfry.nerdnews.business.dagger.components.NewsComponent
import com.chrisfry.nerdnews.business.presenters.NewsListPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.INewsListPresenter
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class PresenterModule(private val newsComponent: NewsComponent) {
    @Provides
    @Singleton
    fun providesNewsListPresenter(): INewsListPresenter {
        return NewsListPresenter.getInstance(newsComponent)
    }
}