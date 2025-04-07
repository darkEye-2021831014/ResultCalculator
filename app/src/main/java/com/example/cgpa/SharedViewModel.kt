package com.example.cgpa

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

class SharedViewModel : ViewModel() {

    val userData = MutableLiveData<MutableList<ItemInfo>>(mutableListOf())
    val budgetData = MutableLiveData<MutableList<BudgetItem>>(mutableListOf())
    val monthlyData = MutableLiveData<MutableMap<Pair<Int,Int>,MonthlyInfo>>(mutableMapOf())
    val monthlyCategoryData = MutableLiveData<MutableMap<Triple<Int,Int,String>,MonthlyInfo>>(mutableMapOf())

    val selectedItem = MutableLiveData<ItemInfo>()
    val selectedChart = MutableLiveData<ChartInfo>()
    val userAccount = MutableLiveData<AccountInfo>()

    // Add data to the respective list
    fun setData(data: Any) {
        val user = FirebaseAuth.getInstance().currentUser
        when (data) {
            is ItemInfo -> {
                val list = userData.value ?: mutableListOf()
                list.add(data)
                sortUserData()
                //update monthlyData for each new item
                updateMonthlyData(data,true)

                user?.let{
                    viewModelScope.launch {
                        Helper.uploadListToFirestoreSuspend(user.uid,list,Helper.ITEM_INFO_COLLECTION)
                    }
                }
            }
            is MonthlyInfo -> {
                val currentMap = monthlyData.value ?: mutableMapOf()
                currentMap[data.month to data.year] = data
                monthlyData.value = currentMap
            }
            is BudgetItem ->{
                val list =  budgetData.value ?: mutableListOf()
                list.add(data)
                list.sortBy { it.isCategory }
                budgetData.value = list
            }
        }
    }

    fun addItem(item:Any, context:Context){
        val user = FirebaseAuth.getInstance().currentUser
        when(item){
            is BudgetItem ->{
                val list =  budgetData.value ?: mutableListOf()
                list.add(item)
                list.sortBy { it.isCategory }
                budgetData.value = list
                Helper.saveList(budgetData,context,Helper.BUDGET_ITEM_FILE)

                user?.let{
                    viewModelScope.launch {
                        Helper.uploadListToFirestoreSuspend(user.uid,list,Helper.BUDGET_ITEM_COLLECTION)
                    }
                }
            }
        }
    }


    // Remove data from the respective list
    fun removeData(data: Any) {
        val user = FirebaseAuth.getInstance().currentUser
        when (data) {
            is ItemInfo -> {
                removeLiveDataItem(userData, data)

                user?.let{
                    viewModelScope.launch {
                        Helper.uploadListToFirestoreSuspend(user.uid,userData.value?.toList()?: listOf(),Helper.ITEM_INFO_COLLECTION)
                    }
                }
            }
            is MonthlyInfo -> removeLiveDataItem(monthlyData, data)
            is BudgetItem -> {
                if(budgetData.value?.contains(data) == true)
                    removeLiveDataItem(budgetData,data)
            }
        }
    }

    fun removeItem(item:Any,context:Context){
        val user = FirebaseAuth.getInstance().currentUser
        when(item){
            is BudgetItem->{
                if(budgetData.value?.contains(item) == true) {
                    removeLiveDataItem(budgetData, item)
                    Helper.saveList(budgetData, context, Helper.BUDGET_ITEM_FILE)

                    user?.let{
                        viewModelScope.launch {
                            Helper.uploadListToFirestoreSuspend(user.uid,budgetData.value?.toList()?: listOf(),Helper.BUDGET_ITEM_COLLECTION)
                        }
                    }
                }
            }
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
    }

    // Generic function to update LiveData by adding an item
    private fun <T> updateLiveData(liveData: MutableLiveData<MutableList<T>>, item: T) {
        val list = liveData.value ?: mutableListOf()
        list.add(item)
        liveData.value = list
    }

    // Generic function to remove an item from LiveData
    private fun <T> removeLiveDataItem(liveData: MutableLiveData<MutableList<T>>, item: T) {
        val list = liveData.value ?: return
        list.remove(item)
        liveData.value = list

        if(item is ItemInfo){
            updateMonthlyData(item,false)
        }
    }

    fun updateMonthlyData(data:ItemInfo,isAdded:Boolean){
        val map = monthlyData.value?: mutableMapOf()
        val categoryMap = monthlyCategoryData.value?: mutableMapOf()
        if(data.isExpense) {
            map.getOrPut(data.month to data.year) {
                MonthlyInfo(data.month, data.monthName, data.year, 0, 0)
            }.expense += if (isAdded) abs(data.amount) else -abs(data.amount)

            categoryMap.getOrPut(Triple(data.month,data.year, data.name)) {
                MonthlyInfo(data.month, data.monthName, data.year, 0, 0)
            }.expense += if (isAdded) abs(data.amount) else -abs(data.amount)
        }
        else {
            map.getOrPut(data.month to data.year) {
                MonthlyInfo(data.month, data.monthName, data.year, 0, 0)
            }.income += if (isAdded) abs(data.amount) else -abs(data.amount)

            categoryMap.getOrPut(Triple(data.month, data.year,data.name)) {
                MonthlyInfo(data.month, data.monthName, data.year, 0, 0)
            }.expense += if (isAdded) abs(data.amount) else -abs(data.amount)
        }
        monthlyData.value = map
        monthlyCategoryData.value = categoryMap
    }


    // Generic function to clear a LiveData list
    private fun <T> clearLiveData(liveData: MutableLiveData<MutableList<T>>) {
        liveData.value = mutableListOf()
    }


    inline fun <reified T> addList(liveData: MutableLiveData<MutableList<T>>, newList:MutableList<T>){
        liveData.value = newList
        if(T::class == ItemInfo::class){
            clearMonthlyData()
            userData.value?.let{
                it.forEach {
                    item->
                    updateMonthlyData(item,true)
                }
            }
            sortUserData()
        }
        else if(T::class == BudgetItem::class){
            val list = budgetData.value?: mutableListOf()
            list.sortBy { it.isCategory }
            budgetData.value= list
        }
    }

}
