package com.chrisfry.nerdnews.userinterface.adapters.holders

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.userinterface.interfaces.ItemSelectionListener

/**
 * View holder for displaying an article object in a recycler view
 */
class ArticleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // UI Elements
    private val articleImage: ImageView = itemView.findViewById(R.id.iv_article_image)
    private val titleText = itemView.findViewById<TextView>(R.id.tv_article_title_text)
    private val sourceText = itemView.findViewById<TextView>(R.id.tv_source_text)

    // Listener reference for article selection (click)
    var listener: ItemSelectionListener? = null

    init {
        itemView.setOnClickListener { listener?.onItemSelected(adapterPosition) }
    }

    fun setImageUrl(fragment: Fragment, url: String) {
        if (url.isEmpty()) {
            articleImage.visibility = View.INVISIBLE
        } else {
            val options = RequestOptions()

            Glide.with(fragment)
                .load(url)
                .apply(options.centerCrop())
                .into(articleImage)
        }
    }

    fun setArticleTitle(title: String) {
        titleText.text = title
    }

    fun setSourceName(sourceName: String) {
        if (sourceName.isEmpty()) {
            sourceText.visibility = View.GONE
        } else {
            sourceText.text = sourceName
        }
    }
}