package com.chrisfry.nerdnews.userinterface.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.userinterface.interfaces.ITabsProvider

/**
 * Fragment for displaying a single article
 */
class ArticleItemFragment : Fragment() {
    companion object {
        private val TAG = ArticleItemFragment::class.java.name
    }

    // UI Elements
    private lateinit var articleImage: ImageView
    private lateinit var titleText: TextView
    private lateinit var authorText: TextView
    private lateinit var publishedAtText: TextView
    private lateinit var contentText: TextView

    // Data that will be displayed
    private var articleToDisplay: ArticleDisplayModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(false)

        val args = arguments
        if (args == null) {
            Log.e(TAG, "Error article item fragment has no arguments")
        } else {
            val articleToDisplay: ArticleDisplayModel? = args.getParcelable(AppConstants.KEY_ARGS_ARTICLE)
            if (articleToDisplay == null) {
                Log.e(TAG, "Error article has not been sent to fragment")
            } else {
                this.articleToDisplay = articleToDisplay
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_article_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Retrieve UI elements references
        articleImage = view.findViewById(R.id.iv_article_item_image)
        titleText = view.findViewById(R.id.tv_article_item_title_text)
        authorText = view.findViewById(R.id.tv_article_item_author_text)
        publishedAtText = view.findViewById(R.id.tv_article_item_published_at_text)
        contentText = view.findViewById(R.id.tv_article_item_content_text)


        val article = articleToDisplay
        if (article != null) {
            val parentActivity = activity
            if (parentActivity == null || parentActivity !is ITabsProvider || parentActivity !is AppCompatActivity) {
                Log.e(TAG, "Error haven't been sent a article to display")
            } else {
                // Set source into title, display back arrow, and hide tabs below toolbar
                parentActivity.supportActionBar?.title = article.sourceName
                parentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
                parentActivity.hideTabs()
            }

            // Display or hide image view
            if (article.imageUrl.isEmpty()) {
                articleImage.visibility = View.GONE
            } else {
                Glide.with(this).load(article.imageUrl).apply(RequestOptions().centerCrop()).into(articleImage)
            }

            // Display title (or placeholder if empty)
            if (article.title.isEmpty()) {
                titleText.text = getString(R.string.no_title_string)
            } else {
                titleText.text = article.title
            }

            // Display or hide author text
            showOrHideTextView(authorText, article.author)

            // Display or hide published at date
            showOrHideTextView(publishedAtText, article.publishedAt)

            // Display content of article (or hide if empty)
            showOrHideTextView(contentText, article.articleContent)
        } else {
            Log.e(TAG, "Error we don't have an article to display in onViewCreated")
        }
    }

    /**
     * Displays provided text in a text view or hides the text view if it's empty
     *
     * @param textView: View to show/hide
     * @param textValue: Value to display in the textview (if not empty)
     */
    private fun showOrHideTextView(textView: TextView, textValue: String) {
        if (textValue.isEmpty()) {
            textView.visibility = View.GONE
        } else {
            textView.text = textValue
        }
    }
}