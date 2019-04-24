package com.chrisfry.nerdnews.business.dagger.modules

import com.chrisfry.nerdnews.model.ArticleDataModel
import com.chrisfry.nerdnews.model.IArticleDataModel
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ArticleModelModule {

    @Provides
    @Singleton
    fun provideArticleModel(): IArticleDataModel {
        return ArticleDataModel.getInstance()
    }
}