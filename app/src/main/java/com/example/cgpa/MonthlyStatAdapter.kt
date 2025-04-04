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
import com.itextpdf.kernel.geom.Line
import org.slf4j.helpers.Util

class MonthlyStatAdapter(
    private val itemList: MutableList<Any>,
) : RecyclerView.Adapter<MonthlyStatAdapter.ItemViewHolder>() {

    // ViewHolder - Holds views
    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val yearText: TextView = itemView.findViewById(R.id.year)
        val yearContainer: LinearLayout = itemView.findViewById(R.id.yearContainer)
        val statContainer: LinearLayout = itemView.findViewById(R.id.statContainer)

        val month: TextView = itemView.findViewById(R.id.month)
        val expense: TextView = itemView.findViewById(R.id.expense)
        val income: TextView = itemView.findViewById(R.id.income)
        val balance: TextView = itemView.findViewById(R.id.balance)
    }

    // Inflate layout for each item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.statistics_item, parent, false)
        return ItemViewHolder(view)
    }

    // Bind data to views
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]

        if(item is Triple<*,*,*>){
            holder.yearContainer.visibility = View.GONE
            holder.statContainer.visibility = View.VISIBLE
            val (monthName,expenseValue,incomeValue) = item
            if(expenseValue is Long && incomeValue is Long && monthName is String){
                val balance = (expenseValue-incomeValue)
                holder.expense.text = Utility.formatedValue(expenseValue)
                holder.income.text = Utility.formatedValue(incomeValue)
                holder.balance.text = Utility.formatedValue(balance)
                holder.month.text = monthName
            }
        }
        else if ( item is Int){
            holder.yearContainer.visibility = View.VISIBLE
            holder.statContainer.visibility = View.GONE
            holder.yearText.text = item.toString()
        }

        holder.itemView.setOnClickListener(null)
        holder.itemView.setOnLongClickListener(null)


    }


    override fun getItemCount() = itemList.size
}
