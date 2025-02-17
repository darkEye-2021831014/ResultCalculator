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
    val expenseIncome:String
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
