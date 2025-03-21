package com.example.cgpa

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ItemAdapter(
    private val itemList: MutableList<Any>,
    private val isClickable: Boolean,
    private val onEditClick: (Any) -> Unit,
    private val onDeleteClick: (Any) -> Unit,
    private val onDetailsClick: (Any) -> Unit,
    private val onDateClick:(Any) ->Unit
) : RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    // ViewHolder - Holds views
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemDate: TextView = itemView.findViewById(R.id.recordDate)
        val dailyExpense: TextView = itemView.findViewById(R.id.expenseDaily)

        val itemName: TextView = itemView.findViewById(R.id.itemName)
        val itemValue: TextView = itemView.findViewById(R.id.itemValue)

        val itemLayout: View = itemView.findViewById(R.id.item)
        val dateLayout: View = itemView.findViewById(R.id.dailyDate)

        val dItem:View = itemView.findViewById(R.id.dItem)

    }

    // Inflate layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.record_item, parent, false)
        return ItemViewHolder(view)
    }

    // Bind data to views
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        // Reset visibility and click listeners to prevent incorrect behavior due to view recycling
        holder.dateLayout.visibility = View.GONE
        holder.itemLayout.visibility = View.GONE
        holder.itemView.setOnClickListener(null)
        holder.itemView.setOnLongClickListener(null)

        if (item is Item) {
            holder.itemLayout.visibility = View.VISIBLE

            holder.itemName.text = item.name
            holder.itemValue.text = item.value
            setDrawableStart(holder, item.icon)

            if (isClickable) {
                holder.itemView.setOnLongClickListener {
                    showOptionsDialog(holder.itemView.context, item)
                    true
                }

                holder.itemView.setOnClickListener {
                    onDetailsClick(item)
                }
            }
        } else if (item is Date) {
            holder.dateLayout.visibility = View.VISIBLE

            holder.itemDate.text = item.date
            holder.dailyExpense.text = item.expenseIncome

            // Ensure Date items are NOT clickable
            holder.itemView.setOnClickListener{
                onDateClick(item)
            }
            holder.itemView.setOnLongClickListener(null)


        }
    }


    //set icon to textView itemName
    private fun setDrawableStart(holder: ItemViewHolder,drawable: Drawable?) {
        holder.itemName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }

    private fun showOptionsDialog(context: Context, item: Any) {
        val options = arrayOf("Edit", "Delete", "Details")

        AlertDialog.Builder(context)
            .setTitle("Choose an action")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onEditClick(item)   // Edit
                    1 -> onDeleteClick(item) // Delete
                    2 -> onDetailsClick(item) // Details
                }
            }
            .show()
    }

    override fun getItemCount() = itemList.size
}
