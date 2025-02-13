package com.example.cgpa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    // MutableLiveData now holds a list of ItemInfo
    val userData = MutableLiveData<MutableList<ItemInfo>>().apply { value = mutableListOf() }

    // Function to set data (add a list or new item)
    fun setUserData(data: ItemInfo) {
        userData.value?.add(data)
        userData.value = userData.value // Trigger LiveData update
    }

    // Function to remove an item from the list
    fun removeItem(data: ItemInfo) {
        userData.value?.remove(data)
        userData.value = userData.value // Trigger LiveData update
    }

    // Optional: Function to clear all items
    fun clearItems() {
        userData.value?.clear()
        userData.value = userData.value // Trigger LiveData update
    }

    fun sortDataByDate() {
        userData.value = userData.value?.sortedByDescending { it.date }?.toMutableList()
    }
}
