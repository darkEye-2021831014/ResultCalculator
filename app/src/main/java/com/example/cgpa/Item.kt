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
    val icon:String?,
    val amount:Long,
    val isExpense:Boolean,
    val date:Int,
    val month:Int,
    val year: Int,
    val monthName:String,
    val dateName:String,
    val note:String?,
)

data class DailyExchange(
    val date:String,
    var expense:Long,
    var income:Long,
)
