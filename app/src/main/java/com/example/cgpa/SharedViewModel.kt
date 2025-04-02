package com.example.cgpa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.abs

class SharedViewModel : ViewModel() {

    val userData = MutableLiveData<MutableList<ItemInfo>>(mutableListOf())
    val budgetData = MutableLiveData<MutableList<BudgetItem>>(mutableListOf())
    val monthlyData = MutableLiveData<MutableMap<Pair<Int,Int>,MonthlyInfo>>(mutableMapOf())

    val selectedItem = MutableLiveData<ItemInfo>()
    val selectedChart = MutableLiveData<ChartInfo>()

    // Add data to the respective list
    fun setData(data: Any) {
        when (data) {
            is ItemInfo -> {
                val list = userData.value ?: mutableListOf()
                list.add(data)
                sortUserData()
                //update monthlyData for each new item
                updateMonthlyData(data,true)
            }
            is MonthlyInfo -> {
                val currentMap = monthlyData.value ?: mutableMapOf()
                currentMap[data.month to data.year] = data
                monthlyData.value = currentMap
            }
            is BudgetItem ->{
                val list =  budgetData.value ?: mutableListOf()
                list.add(data)
//                budgetData.postValue(budgetData.value)
                budgetData.value = list
            }
        }
    }


    // Remove data from the respective list
    fun removeData(data: Any) {
        when (data) {
            is ItemInfo -> removeLiveDataItem(userData, data)
            is MonthlyInfo -> removeLiveDataItem(monthlyData, data)
            is BudgetItem -> removeLiveDataItem(budgetData,data)
        }
    }

    private fun removeLiveDataItem(liveData: MutableLiveData<MutableMap<Pair<Int,Int>,MonthlyInfo>>, item: MonthlyInfo) {
        val map = liveData.value ?: return
        map.remove(item.month to item.year)
        monthlyData.value = map
    }

    // Clear all user data
    fun clearUserData() {
        clearLiveData(userData)
        clearMonthlyData()
    }
    fun clearMonthlyData(){
        monthlyData.value = mutableMapOf()
    }
    fun clearBudgetData() = clearLiveData(budgetData)

    // Sort userData by date in descending order
    fun sortUserData() {
        userData.value = userData.value
            ?.sortedWith(compareByDescending<ItemInfo> { it.year }
                .thenByDescending { it.month }
                .thenByDescending { it.date })
            ?.toMutableList()
        userData.postValue(userData.value) // Ensures LiveData updates correctly
//        userData.value = userData.value
    }

    // Generic function to update LiveData by adding an item
    private fun <T> updateLiveData(liveData: MutableLiveData<MutableList<T>>, item: T) {
        val list = liveData.value ?: mutableListOf()
        list.add(item)
//        liveData.postValue(list) // Ensures LiveData updates on the main thread
        liveData.value = list
    }

    // Generic function to remove an item from LiveData
    private fun <T> removeLiveDataItem(liveData: MutableLiveData<MutableList<T>>, item: T) {
        val list = liveData.value ?: return
        list.remove(item)
//        liveData.postValue(list)
        liveData.value = list

        if(item is ItemInfo){
            updateMonthlyData(item,false)
        }
    }

    private fun updateMonthlyData(data:ItemInfo,isAdded:Boolean){
        val map = monthlyData.value?: mutableMapOf()
        if(data.isExpense)
            map.getOrPut(data.month to data.year) {
                MonthlyInfo(data.month, data.monthName, data.year, 0, 0)
            }.expense += if (isAdded) abs(data.amount) else -abs(data.amount)
        else
            map.getOrPut(data.month to data.year) {
                MonthlyInfo(data.month, data.monthName, data.year, 0, 0)
            }.income += if (isAdded) abs(data.amount) else -abs(data.amount)
        monthlyData.value = map
    }


    // Generic function to clear a LiveData list
    private fun <T> clearLiveData(liveData: MutableLiveData<MutableList<T>>) {
//        liveData.postValue(mutableListOf())
        liveData.value = mutableListOf()
    }

}
