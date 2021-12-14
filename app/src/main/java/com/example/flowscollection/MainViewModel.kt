package com.example.flowscollection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // basically a flow is a coroutine that can emit different values overtime

    // this is called cold flow because does nothing and needs something to collect it and start it
    val countDownFlow = flow<Int> {
        val startingValue = 10
        var currentValue = startingValue
        emit(startingValue)

        while (currentValue > 0) {
            delay(1000)
            currentValue--
            emit(currentValue) // we inform that it is ready to be processed to any thing that uses it
        }
    }

    init {
        collectFlow()
    }

    // just for test, don't use it this way on real app for UI, for viewModel process may be ok
    private fun collectFlow() {
        viewModelScope.launch {
            // this collect gets called every time the flow emits something
            // retrieves all the values even if it is delayed
            countDownFlow.collect { time ->
                delay(1500)
                println("The current time is $time")
            }
        }
    }

    private fun collectFlowLatest() {
        viewModelScope.launch {
            // this collect gets called every time the flow emits something but if we still
            // processing something it gets cancelled and starts with the new emission
            countDownFlow.collectLatest { time ->
                delay(1500)
                println("The current time is $time")
            }
        }
    }


}