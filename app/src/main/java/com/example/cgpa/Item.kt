package com.example.cgpa

import android.graphics.drawable.Drawable
import android.net.Uri

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
    var name:String="",
    var icon:String?=null,
    var amount:Long=0L,
    var isExpense:Boolean=false,
    var date:Int=1,
    var month:Int=1,
    var year: Int=2000,
    var monthName:String="Jan",
    var dateName:String="",
    var note:String?=null,
    var id:String?=null
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
    var heading:String="",
    var startIcon:String?=null,
    var endIcon:String?=null,
    var budget:Long=0L,
    var expense:Long=0L,
    var isCategory:Boolean=false,
    var id:String?=null
)

data class MonthlyInfo(
    val month:Int,
    val monthName:String,
    val year:Int,
    var expense:Long,
    var income:Long,
)

data class AccountInfo(
    var name:String?,
    var email:String?,
    var photoUrl: String?,
    var uid:String,
    var signInButtonText:String,
    var buttonColor:Int,
    var startIcon:Drawable?,
    var endIcon: Drawable?
)

data class SharedItem(
    var name:String? = null,
    var owner:String? = null,
    var emails:MutableList<String> = mutableListOf(),
    var id:String?=null,
    var items:MutableList<ItemInfo> = mutableListOf(),
    var budgets:MutableList<BudgetItem> = mutableListOf()
)
