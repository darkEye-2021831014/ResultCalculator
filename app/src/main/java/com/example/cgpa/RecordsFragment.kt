package com.example.cgpa

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.IOException
import kotlin.math.abs

class RecordsFragment : Fragment() {

    private val viewModel by activityViewModels<SharedViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_records, container, false)
        //load saved data
        loadItemInfo();

        // Handle menu button click
        val menu: ImageButton = view.findViewById(R.id.menu)
        val drawer: DrawerLayout = view.findViewById(R.id.drawer_Layout_Records)
        menu.setOnClickListener { drawer.openDrawer(GravityCompat.START) }

        // Navigate to Result Calculator
        view.findViewById<Button>(R.id.resultCalculator).setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }

        // Exit App
        view.findViewById<Button>(R.id.exit).setOnClickListener {
            requireActivity().finishAffinity()
        }

        setupRecyclerView(view)

        return view
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.recordContainer)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.userData.observe(viewLifecycleOwner) { data ->
            val items = processUserData(data)

            recyclerView.adapter = ItemAdapter(
                items,
                onEditClick = { item -> showToast("Editing: $item") },
                onDeleteClick = { item -> deleteItem(item as Item) },
                onDetailsClick = { item -> itemDetails(item as Item) }
            )
        }
    }


    private fun processUserData(data: List<ItemInfo>): MutableList<Any> {
        val items = mutableListOf<Any>()
        var date = -1
        val tmp = mutableListOf<Item>()
        var expense = 0L
        var income = 0L
        var dateText = ""

        for (item in data) {
            if (item.date != date) {
                if (date != -1) {
                    addDateHeader(items, dateText, expense, income)
                    items.addAll(tmp)
                    tmp.clear()
                }
                dateText = getDateText(item)
                date = item.date
                expense = 0L
                income = 0L
            }

            tmp.add(Item(item.note ?: item.name, Helper.loadIcon(item.icon,requireContext()), item.amount,item))
            if (item.isExpense) expense += abs(item.amount)
            else income += abs(item.amount)
        }

        if (date != -1) {
            addDateHeader(items, dateText, expense, income)
            items.addAll(tmp)
        }

        return items
    }

    private fun addDateHeader(items: MutableList<Any>, dateText: String, expense: Long, income: Long) {
        val summary = when {
            expense == 0L -> "Income: $income"
            income == 0L -> "Expense: $expense"
            else -> "Expense: $expense  Income: $income"
        }
        items.add(Date(dateText, summary))
    }

    private fun getDateText(itemInfo: ItemInfo): String {
        return "${itemInfo.monthName} ${itemInfo.date}  ${itemInfo.dateName}"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }



    private fun loadItemInfo()
    {
//        Helper.deleteAllSavedIcons(requireContext())
//        Helper.saveItemInfoList(viewModel.userData,requireContext());

        val itemInfo = Helper.retrieveItemInfo(requireContext());
        for(item in itemInfo)
        {
            viewModel.setData(item);
        }
    }


    fun deleteItem(item:Item)
    {
        val itemInfo = item.info
        Helper.showConfirmationDialog(requireContext(), "Are you sure you want to delete?"){
            confirm->
            if(confirm)
                viewModel.removeData(itemInfo)
        }
    }

    fun itemDetails(item:Item)
    {
        viewModel.selectedItem.value = item.info;
        val bottomSheet = ItemDetailsFregment()
        bottomSheet.show(requireActivity().supportFragmentManager, "ItemDetailsFregment")
    }


}
