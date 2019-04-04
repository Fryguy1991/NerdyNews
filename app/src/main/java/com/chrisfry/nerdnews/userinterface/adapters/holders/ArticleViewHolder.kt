package com.chrisfry.nerdnews.userinterface.adapters.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chrisfry.nerdnews.R

/**
 * View holder for displaying an article object in a recycler view
 */
class ArticleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val articleImage: ImageView = itemView.findViewById(R.id.iv_article_image)
    private val titleText = itemView.findViewById<TextView>(R.id.tv_article_title_text)
    private val sourceText = itemView.findViewById<TextView>(R.id.tv_source_text)

    fun setImageUrl(fragment: Fragment, url: String?) {
        if (url == null) {
            articleImage.visibility = View.GONE
        } else {
            val options = RequestOptions()
            options.centerCrop()

            Glide.with(fragment)
                .load(url)
                .apply(options)
                .into(articleImage)
        }
    }

    fun setArticleTitle(title: String) {
        titleText.text = title
    }

    fun setSourceName(sourceName: String?) {
        if (sourceName == null) {
            sourceText.visibility = View.GONE
        } else {
            sourceText.text = sourceName
        }
    }
}