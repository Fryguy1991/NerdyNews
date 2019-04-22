package com.chrisfry.nerdnews.userinterface.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chrisfry.nerdnews.AppConstants
import com.chrisfry.nerdnews.R
import com.chrisfry.nerdnews.business.presenters.ArticleItemPresenter
import com.chrisfry.nerdnews.business.presenters.interfaces.IArticleItemPresenter
import com.chrisfry.nerdnews.model.ArticleDisplayModelParcelable
import com.chrisfry.nerdnews.userinterface.interfaces.ITabsProvider
import com.chrisfry.nerdnews.utils.LogUtils
import kotlinx.android.synthetic.main.fragment_article_item.*

/**
 * Fragment for displaying a single article
 */
class ArticleItemFragment : Fragment(), ArticleItemPresenter.IArticleItemView, View.OnClickListener {
    companion object {
        private val TAG = ArticleItemFragment::class.java.simpleName

        /**
         * Method for creating an instance of ArticleItemFragment
         *
         * @param articleToDisplay: The parcelable model of the article we want to display
         */
        fun getInstance (articleToDisplay: ArticleDisplayModelParcelable): ArticleItemFragment {
            val fragment = ArticleItemFragment()
            val args = Bundle()
            args.putParcelable(AppConstants.KEY_ARGS_ARTICLE, articleToDisplay)
            fragment.arguments = args

            return fragment
        }
    }

    // Reference to presenter that provides data
    private var presenter: IArticleItemPresenter? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        presenter = ArticleItemPresenter.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(false)

        // Setup presenter with article argument (or null if not sent one)
        val args = arguments
        if (args == null) {
            LogUtils.error(TAG, "Error article item fragment has no arguments")
            presenter?.setArticleData(null)
        } else {
            val articleToDisplay: ArticleDisplayModelParcelable? = args.getParcelable(AppConstants.KEY_ARGS_ARTICLE)
            presenter?.setArticleData(articleToDisplay)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_article_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Setup toolbar for current fragment
        val parentActivity = activity
        if (parentActivity == null || parentActivity !is ITabsProvider || parentActivity !is AppCompatActivity) {
            LogUtils.error(TAG, "Invalid activity to change support action bar title")
        } else {
            // Display back arrow and hide tabs below toolbar
            parentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            parentActivity.hideTabs()
        }

        // Add click listener to button to view full article
        btn_go_to_article.setOnClickListener(this)

        presenter?.attach(this)
    }

    override fun onDestroyView() {
        presenter?.detach()
        super.onDestroyView()
    }

    override fun onDestroy() {
        presenter = null
        super.onDestroy()
    }

    override fun displaySourceName(sourceName: String) {
        val parentActivity = activity
        if (parentActivity == null || parentActivity !is AppCompatActivity) {
            LogUtils.error(TAG, "Error invalid activity to change support action bar title")
        } else {
            // Set source into title
            parentActivity.supportActionBar?.title = sourceName
        }
    }

    override fun displayTitle(title: String) {
        // Display title (or placeholder if empty)
        if (title.isEmpty()) {
            tv_article_item_title_text.text = getString(R.string.no_title_string)
        } else {
            tv_article_item_title_text.text = title
        }
    }

    override fun displayImage(imageUrl: String) {
        // Display or hide image view
        if (imageUrl.isEmpty()) {
            iv_article_item_image.visibility = View.GONE
        } else {
            Glide.with(this).load(imageUrl).apply(RequestOptions().centerCrop()).into(iv_article_item_image)
        }
    }

    override fun displayAuthor(author: String) {
        // Display or hide author text
        showOrHideTextView(tv_article_item_author_text, author)
    }

    override fun displayPublishedAt(publishedAt: String) {
        // Display or hide published at date
        showOrHideTextView(tv_article_item_published_at_text, publishedAt)
    }

    override fun displayContent(content: String) {
        // Display content of article (or hide if empty)
        showOrHideTextView(tv_article_item_content_text, content)
    }

    override fun displayLinkToArticle(articleUrl: String) {
        if (articleUrl.isEmpty()) {
            btn_go_to_article.visibility = View.GONE
        } else {
            btn_go_to_article.visibility = View.VISIBLE
        }
    }

    override fun closeView() {
        // Presenter has indicated that we need to close the view (article is null)
        val manager = fragmentManager
        if (manager == null) {
            LogUtils.error(TAG, "Error fragment manager is null")
        } else {
            val currentContext = context
            if (currentContext != null) {
                Toast.makeText(currentContext, R.string.toast_invalid_article_message, Toast.LENGTH_LONG).show()
            } else {
                LogUtils.error(TAG, "Can't toast from view. Article invalid")
            }
            manager.popBackStack()
        }
    }

    override fun navigateToArticleSource(articleUrl: String) {
        if (articleUrl.isNotEmpty()) {
            // If our URL is not blank, launch an intent to open the article URL in a web browser
            val browserIntent = Intent(Intent.ACTION_VIEW)
            browserIntent.data = Uri.parse(articleUrl)
            startActivity(browserIntent)
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

    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id) {
                btn_go_to_article.id -> {
                    presenter?.goToArticleClicked()
                }
                else -> {
                    // Not handling this click here
                }
            }
        }
    }
}