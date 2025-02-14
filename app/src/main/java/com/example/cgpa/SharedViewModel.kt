package com.example.cgpa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    val userData = MutableLiveData<MutableList<ItemInfo>>(mutableListOf())
    val dailyExchange = MutableLiveData<MutableList<DailyExchange>>(mutableListOf())
    val selectedItem = MutableLiveData<ItemInfo>()

    // Add data to the respective list
    fun setData(data: Any) {
        when (data) {
            is ItemInfo -> {
                val list = userData.value ?: mutableListOf()
                list.add(data)
                sortUserData()
            }
            is DailyExchange -> updateLiveData(dailyExchange, data)
        }
    }


    // Remove data from the respective list
    fun removeData(data: Any) {
        when (data) {
            is ItemInfo -> removeLiveDataItem(userData, data)
            is DailyExchange -> removeLiveDataItem(dailyExchange, data)
        }
    }

    // Clear all user data
    fun clearUserData() = clearLiveData(userData)

    // Clear all daily expenses
    fun clearDailyExpense() = clearLiveData(dailyExchange)

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
        liveData.postValue(list) // Ensures LiveData updates on the main thread
    }

    // Generic function to remove an item from LiveData
    private fun <T> removeLiveDataItem(liveData: MutableLiveData<MutableList<T>>, item: T) {
        val list = liveData.value ?: return
        list.remove(item)
        liveData.postValue(list)
    }

    // Generic function to clear a LiveData list
    private fun <T> clearLiveData(liveData: MutableLiveData<MutableList<T>>) {
        liveData.postValue(mutableListOf())
    }

    //date format "YYYY-MM-DD"
    fun updateExchange(date: String, amount: Long, isExpense: Boolean) {
        val existingData = dailyExchange.value?.find { it.date == date }

        if (existingData != null) {
            // If data for the date exists, update either expense or income
            if (isExpense) {
                existingData.expense += amount
            } else {
                existingData.income += amount
            }
        } else {
            // If data for the date doesn't exist, create a new entry
            val newData = DailyExchange(date, expense = if (isExpense) amount else 0, income = if (!isExpense) amount else 0)
            dailyExchange.value?.add(newData)
        }

        // Post the updated list back to LiveData
        dailyExchange.postValue(dailyExchange.value)
    }

}
