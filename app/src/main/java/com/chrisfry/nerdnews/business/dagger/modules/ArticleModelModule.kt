package com.chrisfry.nerdnews.business.dagger.modules

import com.chrisfry.nerdnews.model.ArticleListsModel
import com.chrisfry.nerdnews.model.IArticleListsModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ArticleModelModule {

    @Provides
    @Singleton
    fun provideArticleModel(): IArticleListsModel {
        return ArticleListsModel.getInstance()
    }
}