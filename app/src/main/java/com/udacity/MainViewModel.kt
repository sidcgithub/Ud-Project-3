package com.udacity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {

    val checkFailure: MutableLiveData<Int> = MutableLiveData()

    init {
        checkFailure.value = 0
    }

    fun downloadingCheck() {
        viewModelScope.launch {
            delay(1000)
            checkFailure.value = checkFailure.value?.plus(1) ?: 0
        }
    }

}