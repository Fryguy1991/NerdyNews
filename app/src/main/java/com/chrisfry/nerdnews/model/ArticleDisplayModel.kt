package com.chrisfry.nerdnews.model

/**
 * Simpler model of Article used for displaying data retrieved from NewsAPI.
 */
data class ArticleDisplayModel(
    val title: String,
    val sourceName: String,
    val imageUrl: String,
    val author: String,
    val articleUrl: String,
    val articleContent: String,
    val publishedAt: String
)