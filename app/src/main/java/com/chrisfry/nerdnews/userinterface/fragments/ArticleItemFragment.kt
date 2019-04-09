package com.chrisfry.nerdnews.userinterface.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.chrisfry.nerdnews.model.ArticleDisplayModel
import com.chrisfry.nerdnews.userinterface.App
import com.chrisfry.nerdnews.userinterface.interfaces.ITabsProvider
import java.lang.Exception
import javax.inject.Inject

/**
 * Fragment for displaying a single article
 */
class ArticleItemFragment : Fragment(), ArticleItemPresenter.IArticleItemView {
    companion object {
        private val TAG = ArticleItemFragment::class.java.name
    }

    // Reference to presenter that provides data
    @Inject
    lateinit var presenter: IArticleItemPresenter

    // UI Elements
    private lateinit var articleImage: ImageView
    private lateinit var titleText: TextView
    private lateinit var authorText: TextView
    private lateinit var publishedAtText: TextView
    private lateinit var contentText: TextView

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val parentActivity = activity
        if (parentActivity == null) {
            throw Exception("Error invalid activity provided")
        } else {
            // Inject presenter from presenter component
            val presenterComponent = (parentActivity.application as App).presenterComponent
            presenterComponent.inject(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(false)

        // Setup presenter with article argument (or null if not sent one)
        val args = arguments
        if (args == null) {
            Log.e(TAG, "Error article item fragment has no arguments")
            presenter.setArticleData(null)
        } else {
            val articleToDisplay: ArticleDisplayModel? = args.getParcelable(AppConstants.KEY_ARGS_ARTICLE)
            presenter.setArticleData(articleToDisplay)
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

        // Setup toolbar for current fragment
        val parentActivity = activity
        if (parentActivity == null || parentActivity !is ITabsProvider || parentActivity !is AppCompatActivity) {
            Log.e(TAG, "Invalid activity to change support action bar title")
        } else {
            // Display back arrow and hide tabs below toolbar
            parentActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
            parentActivity.hideTabs()
        }

        presenter.attach(this)
    }

    override fun onDestroyView() {
        presenter.detach()
        super.onDestroyView()
    }

    override fun displaySourceName(sourceName: String) {
        val parentActivity = activity
        if (parentActivity == null || parentActivity !is AppCompatActivity) {
            Log.e(TAG, "Error invalid activity to change support action bar title")
        } else {
            // Set source into title
            parentActivity.supportActionBar?.title = sourceName
        }
    }

    override fun displayTitle(title: String) {
        // Display title (or placeholder if empty)
        if (title.isEmpty()) {
            titleText.text = getString(R.string.no_title_string)
        } else {
            titleText.text = title
        }
    }

    override fun displayImage(imageUrl: String) {
        // Display or hide image view
        if (imageUrl.isEmpty()) {
            articleImage.visibility = View.GONE
        } else {
            Glide.with(this).load(imageUrl).apply(RequestOptions().centerCrop()).into(articleImage)
        }
    }

    override fun displayAuthor(author: String) {
        // Display or hide author text
        showOrHideTextView(authorText, author)
    }

    override fun displayPublishedAt(publishedAt: String) {
        // Display or hide published at date
        showOrHideTextView(publishedAtText, publishedAt)
    }

    override fun displayContent(content: String) {
        // Display content of article (or hide if empty)
        showOrHideTextView(contentText, content)
    }

    override fun displayLinkToArticle(articleUrl: String) {
        // TODO: Need to implement
    }

    override fun closeView() {
        // Presenter has indicated that we need to close the view (article is null)
        val manager = fragmentManager
        if (manager == null) {
            Log.e(TAG, "Error fragment manager is null")
        } else {
            val currentContext = context
            if (currentContext != null) {
                Toast.makeText(currentContext, R.string.invalid_article_message, Toast.LENGTH_LONG).show()
            } else {
                Log.e(TAG, "Can't toast from view. Article invalid")
            }
            manager.popBackStack()
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