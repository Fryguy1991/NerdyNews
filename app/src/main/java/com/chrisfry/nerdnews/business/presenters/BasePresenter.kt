package com.chrisfry.nerdnews.business.presenters

import com.chrisfry.nerdnews.business.presenters.interfaces.IBasePresenter
import com.chrisfry.nerdnews.userinterface.interfaces.IView

/**
 * Base presenter class handle attaching/detaching view object reference
 *
 * @param T: Generic view object type that extends IView
 */
open class BasePresenter<T : IView> : IBasePresenter<T> {
    // Reference to presenter's view
    private var presenterView: T? = null

    override fun attach(view: T) {
        this.presenterView = view
    }

    override fun detach() {
        presenterView = null
    }

    /**
     * Provides attached view of provided generic type (or null)
     *
     * @return: If attached, view object of provided generic, else null
     */
    protected fun getView(): T? {
        return presenterView
    }
}