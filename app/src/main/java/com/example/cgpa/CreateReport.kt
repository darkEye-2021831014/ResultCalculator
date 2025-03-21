package com.example.cgpa

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.fragment.app.activityViewModels
import java.io.File
import kotlin.math.abs

class CreateReport(private val context:Context,
                   private val activity:Activity,
                   private var reportType:Format,
                   private var date:CalenderDate,
                   private val viewModel: SharedViewModel) {

    private val reportItems:MutableList<reportItem> = mutableListOf()
    private val itemValue:MutableMap<String,Long> = mutableMapOf()
    private val itemDescription:MutableMap<String,String> = mutableMapOf()
    private val descriptionValue:MutableMap<String,Long> = mutableMapOf()


    fun generateReport(reportType:Format, date:CalenderDate)
    {
        this.reportType = reportType
        this.date = date
        createReport()
    }

    private fun expression(item:ItemInfo):Boolean{
        return when(reportType) {
            Format.DAILY_REPORT ->
                item.date == date.day && item.year == date.year && item.month == date.month

            Format.MONTHLY_REPORT ->
                item.year == date.year && item.month == date.month

            Format.YEARLY_REPORT ->
                item.year == date.year

            else ->
                false
        }
    }

    private fun createReport(){
        viewModel.userData.value?.let { userData ->
            //expense data
            userData.filter {
                expression(it) && it.isExpense
            }.forEach { item ->
                populate(item)
            }

            val fullExpenseData = generateReportData()
            val expenseData = reportItems.toList() //create a copy not a reference
            clear() //clear all data for reuse purposes


            //income data
            userData.filter {
                expression(it) && !it.isExpense
            }.forEach { item ->
                populate(item)
            }

            val fullIncomeData = generateReportData()
            val incomeData = reportItems.toList()  //create a copy not a reference
            clear() //clear all data

            //create report
            PdfManager.createReportPdf(context,
                File(File(activity.getExternalFilesDir(null), Helper.FOLDER),Helper.PDF_REPORT_FILE),expenseData,fullExpenseData,reportType,date,incomeData,fullIncomeData)
        }
    }





    private fun populate(item:ItemInfo)
    {
        itemValue[item.name] = (itemValue[item.name] ?: 0L) + abs(item.amount)
        itemDescription[item.note ?: item.name] = item.name
        descriptionValue[item.note ?: item.name] = (descriptionValue[item.note ?: item.name] ?: 0L) + abs(item.amount)
    }


    private fun generateReportData(): List<reportNote>
    {
        val sortedItems = itemValue.entries.sortedByDescending { it.value }
        val sum = sortedItems.sumOf { it.value }

        sortedItems.forEach { (itemName, value) ->
            val percentage = (abs(value) * 100.0 / sum).let { String.format("%.2f", it) }
            reportItems.add(reportItem(itemName, value, "$percentage %"))
        }

        val reportDescriptions = descriptionValue.map { (note, value) ->
            reportNote(note, itemDescription[note] ?: "Other", value)
        }.toMutableList()

        return reportDescriptions
    }

    private fun clear()
    {
        reportItems.clear()
        itemValue.clear()
        itemDescription.clear()
        descriptionValue.clear()
    }
}
