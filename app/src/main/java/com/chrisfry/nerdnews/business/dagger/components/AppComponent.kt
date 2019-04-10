package com.chrisfry.nerdnews.business.dagger.components

import com.chrisfry.nerdnews.business.dagger.modules.ArticleModelModule
import com.chrisfry.nerdnews.business.dagger.modules.NewsModule
import com.chrisfry.nerdnews.business.presenters.ArticleListPresenter
import com.chrisfry.nerdnews.business.presenters.NewsPagingPresenter
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component (
    modules = [NewsModule::class, ArticleModelModule::class]
)
interface AppComponent {
    fun inject(newsPagingPresenter: NewsPagingPresenter)
    fun inject(articleListPresenter: ArticleListPresenter)
}