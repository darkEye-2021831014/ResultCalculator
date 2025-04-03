package com.example.cgpa

import android.graphics.drawable.Drawable

data class Item(
    val name:String,
    val icon:Drawable?,
    val value:String,
    val info:ItemInfo
)

data class Date(
    val date:String,
    val expenseIncome:String,
    val day:Int,
    val month:Int,
    val monthName:String,
    val year:Int
)

data class ItemInfo(
    var name:String,
    var icon:String?,
    var amount:Long,
    var isExpense:Boolean,
    var date:Int,
    var month:Int,
    var year: Int,
    var monthName:String,
    var dateName:String,
    var note:String?,
)

data class DailyExchange(
    val date:String,
    var expense:Long,
    var income:Long,
)

data class ChartInfo(
    val chart:Format,
    val isExpense:Boolean,
)

data class reportItem(
    val category:String,
    val amount:Long,
    val percentage:String
)

data class CalenderDate(
    val day:Int,
    val month: Int,
    val monthName:String,
    val year:Int
)

data class reportNote(
    val note:String,
    val name:String,
    val amount:Long
)

data class BudgetItem(
    val heading:String,
    val startIcon:String?,
    val endIcon:String?,
    var budget:Long,
    var expense:Long,
    val isCategory:Boolean,
)

data class MonthlyInfo(
    val month:Int,
    val monthName:String,
    val year:Int,
    var expense:Long,
    var income:Long,
)
