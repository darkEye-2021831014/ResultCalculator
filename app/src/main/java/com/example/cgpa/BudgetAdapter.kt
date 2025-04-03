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

class BudgetAdapter(
    private val context:Context,
    private val itemList: MutableList<BudgetItem>,
    private val onClick: (BudgetItem) -> Unit,
    private val onLongClick:(BudgetItem) ->Unit,
) : RecyclerView.Adapter<BudgetAdapter.ItemViewHolder>() {

    // ViewHolder - Holds views
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.budgetDescription)
        val pieChart: PieChart = itemView.findViewById(R.id.progressBar)

        val remaining: TextView = itemView.findViewById(R.id.remaining)
        val budget: TextView = itemView.findViewById(R.id.budget)
        val expense: TextView = itemView.findViewById(R.id.expense)

        val budgetItem:LinearLayout = itemView.findViewById(R.id.budgetItem)
    }

    // Inflate layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.budget_item, parent, false)
        return ItemViewHolder(view)
    }

    // Bind data to views
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        val remaining = item.budget-item.expense
        var percent = if (item.budget!=0L && remaining > 0L) (remaining*100.0)/item.budget else 0.0
        percent = Utility.formatDouble(percent)
        val percentage = percent.toFloat()


        holder.itemView.setOnClickListener(null)
        holder.itemView.setOnLongClickListener(null)

        holder.description.text = if(item.isCategory) "Monthly Budget - ${item.heading}" else item.heading
        setDrawableStartEnd(holder,Helper.loadIcon(item.startIcon, context),Helper.loadIcon(item.endIcon, context))

        holder.remaining.text = Utility.formatedValue(remaining)
        holder.budget.text = Utility.formatedValue(item.budget)
        holder.expense.text = Utility.formatedValue(item.expense)

        if(item.budget == 0L)
            Helper.progressBarCircular(holder.pieChart,0F,"--", Color.WHITE)
        else if(remaining<0)
            Helper.progressBarCircular(holder.pieChart,percentage,"Exceed", Color.RED)
        else
            Helper.progressBarCircular(holder.pieChart,percentage,"Remaining\n${percentage}%", Color.WHITE)

        holder.budgetItem.setOnClickListener {
            onClick(item)
        }
        holder.budgetItem.setOnLongClickListener {
            onLongClick(item)
            true
        }
    }


    private fun setDrawableStartEnd(holder: ItemViewHolder,drawableStart: Drawable?,drawableEnd:Drawable?) {
        holder.description.setCompoundDrawablesWithIntrinsicBounds(drawableStart, null, drawableEnd, null)
    }

//    private fun showOptionsDialog(context: Context, item: Any) {
//        val options = arrayOf("Edit", "Delete", "Details")
//
//        AlertDialog.Builder(context)
//            .setTitle("Choose an action")
//            .setItems(options) { _, which ->
//                when (which) {
//                    0 -> onEditClick(item)   // Edit
//                    1 -> onDeleteClick(item) // Delete
//                    2 -> onDetailsClick(item) // Details
//                }
//            }
//            .show()
//    }

    override fun getItemCount() = itemList.size
}
