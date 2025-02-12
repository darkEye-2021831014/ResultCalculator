package com.example.cgpa

import android.graphics.drawable.Drawable

data class Item(
    val name:String,
    val icon:Drawable?,
    val value:String
)

data class Date(
    val date:String,
    val expenseIncome:String
)
