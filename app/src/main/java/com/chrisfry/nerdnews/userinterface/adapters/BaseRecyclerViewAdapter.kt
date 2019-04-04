package com.chrisfry.nerdnews.userinterface.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Base recycler view adapter class which uses generics for item typ (T) and view holder type (V)
 *
 * @param T: Generic representing provided item type
 * @param V: Generic representing provided view holder type
 */
abstract class BaseRecyclerViewAdapter<T, V : RecyclerView.ViewHolder>: RecyclerView.Adapter<V>() {
    // List to hold items to be displayed by the recycler view
    protected var itemList = listOf<T>()

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun updateAdapter(newItemList : List<T>) {
        itemList = newItemList
        notifyDataSetChanged()
    }

    abstract override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V

    abstract override fun onBindViewHolder(holder: V, position: Int)
}