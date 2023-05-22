package com.mobiliteamdhiwisever.app.modules.thanks.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobiliteamdhiwisever.app.R
import com.mobiliteamdhiwisever.app.databinding.RowRandomflowerBinding
import com.mobiliteamdhiwisever.app.modules.thanks.`data`.model.RandomflowerRowModel
import kotlin.Int
import kotlin.collections.List

class RandomFlowerAdapter(
  var list: List<RandomflowerRowModel>
) : RecyclerView.Adapter<RandomFlowerAdapter.RowRandomflowerVH>() {
  private var clickListener: OnItemClickListener? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowRandomflowerVH {
    val view=LayoutInflater.from(parent.context).inflate(R.layout.row_randomflower,parent,false)
    return RowRandomflowerVH(view)
  }

  override fun onBindViewHolder(holder: RowRandomflowerVH, position: Int) {
    val randomflowerRowModel = RandomflowerRowModel()
    // TODO uncomment following line after integration with data source
    // val randomflowerRowModel = list[position]
    holder.binding.randomflowerRowModel = randomflowerRowModel
  }

  override fun getItemCount(): Int = 2
  // TODO uncomment following line after integration with data source
  // return list.size

  public fun updateData(newData: List<RandomflowerRowModel>) {
    list = newData
    notifyDataSetChanged()
  }

  fun setOnItemClickListener(clickListener: OnItemClickListener) {
    this.clickListener = clickListener
  }

  interface OnItemClickListener {
    fun onItemClick(
      view: View,
      position: Int,
      item: RandomflowerRowModel
    ) {
    }
  }

  inner class RowRandomflowerVH(
    view: View
  ) : RecyclerView.ViewHolder(view) {
    val binding: RowRandomflowerBinding = RowRandomflowerBinding.bind(itemView)
  }
}
