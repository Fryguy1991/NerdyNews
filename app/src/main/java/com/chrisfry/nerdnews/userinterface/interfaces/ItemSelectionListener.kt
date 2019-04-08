package com.chrisfry.nerdnews.userinterface.interfaces

/**
 * Interface for passing up the adapter index of the selected item in a recycler view
 */
interface ItemSelectionListener {

    /**
     * Event fired when an item is selected in a recycler view
     *
     * @param adapterPosition: Adapter index of the given element that was selected
     */
    fun onItemSelected(adapterPosition: Int)
}