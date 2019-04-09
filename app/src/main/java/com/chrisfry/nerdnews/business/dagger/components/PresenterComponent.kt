package com.chrisfry.nerdnews.business.dagger.components

import com.chrisfry.nerdnews.business.dagger.modules.PresenterModule
import com.chrisfry.nerdnews.userinterface.fragments.ArticleItemFragment
import com.chrisfry.nerdnews.userinterface.fragments.NewsPagerFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component (
    modules = [PresenterModule::class]
)
interface PresenterComponent {
    fun inject(newsPagerFragment: NewsPagerFragment)
    fun inject(articleItemFragment: ArticleItemFragment)
}