package com.example.cgpa

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.itextpdf.kernel.colors.ColorConstants
import org.slf4j.helpers.Util

class SharedRecordAdapter(
    private val context:Context,
    private val itemList: MutableList<SharedItem>,
    private val onClick: (SharedItem) -> Unit,
    private val onLongClick:(SharedItem) ->Unit,
) : RecyclerView.Adapter<SharedRecordAdapter.ItemViewHolder>() {

    // ViewHolder - Holds views
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.itemDescription)
        val emailList:TextView = itemView.findViewById(R.id.emailList)

        val sharedItem:LinearLayout = itemView.findViewById(R.id.sharedItem)
    }

    // Inflate layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.shared_item, parent, false)
        return ItemViewHolder(view)
    }

    // Bind data to views
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        holder.description.text = item.name
        var list = "N/A";
        for(email in item.emails){
            if(list=="N/A")list = email
            else list+="\n$email"
        }
        holder.emailList.text = list

        holder.itemView.setOnClickListener(null)
        holder.itemView.setOnLongClickListener(null)
        holder.sharedItem.setOnClickListener {
            onClick(item)
        }
        holder.sharedItem.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }


    override fun getItemCount() = itemList.size
}
