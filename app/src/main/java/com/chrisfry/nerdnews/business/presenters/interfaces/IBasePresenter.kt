package com.chrisfry.nerdnews.business.presenters.interfaces

import com.chrisfry.nerdnews.userinterface.interfaces.IView

/**
 * Base presenter type used for MVP architecture
 *
 * @param T: Generic view object that extends IView
 */
interface IBasePresenter<T: IView> {

    /**
     * Connects presenter to the provided view
     *
     * @param view: Generic view object that extends IView
     */
    fun attach(view: T)

    /**
     * Removes references of view object from presenter
     */
    fun detach()

    /**
     * Informs presenter that dependency injection is complete. Allows for any further initiation
     */
    fun postDependencyInitiation()

    /**
     * Informs the presenter that it is about to be destroyed and should start to close
     */
    fun breakDown()
}