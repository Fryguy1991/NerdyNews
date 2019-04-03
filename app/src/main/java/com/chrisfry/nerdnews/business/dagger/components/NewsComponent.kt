package com.chrisfry.nerdnews.business.dagger.components

import com.chrisfry.nerdnews.business.dagger.modules.NewsModule
import com.chrisfry.nerdnews.business.presenters.NewsListPresenter
import com.chrisfry.nerdnews.userinterface.activities.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component (
    modules = [NewsModule::class]
)

interface NewsComponent {
    fun inject(activity: MainActivity)
    fun inject(newsListPresenter: NewsListPresenter)
}