package com.example.flowscollection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
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

    val restaurantFlow = flow {
        delay(250L)
        emit("Appetizer")
        delay(1000L)
        emit("Main dish")
        delay(200L)
        emit("dessert")
    }


    // basically stateFlow is a hot flow that emits whether is being observed or not
    // and also holds the current value which means survives rotation changes and persists
    private val _stateFlow = MutableStateFlow(0)
    val stateFlow = _stateFlow.asStateFlow()

    //StateFlow, it keeps the the most recent state. like for rotating screen
    // SharedFlow is more for replaying previous states, which you don't want for UI state.
    // does not trigger again on rotation

    private val _sharedFlow = MutableSharedFlow<Int>()
    val sharedFlow = _sharedFlow.asSharedFlow()

    fun squareNumber(number: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            _sharedFlow.emit(number * number)
        }
    }

    fun incrementCounter() {
        _stateFlow.value += 1
    }


    init {
        squareNumber(3) // if we use it in here the event is lost because is hot flow and the event
        // happens regarding if observers available

        viewModelScope.launch(Dispatchers.Main) {
            sharedFlow.collect {
                delay(2000L)
                println("FIRST FLOW: The received number is $it")
            }
        }
        viewModelScope.launch(Dispatchers.Main) {
            sharedFlow.collect {
                delay(3000L)
                println("SECOND FLOW: The received number is $it")
            }
        }

        squareNumber(4)
        // after this call the event does not happen again which means is only triggered once

        // with replay param it caches the number given
    }

    private fun flowRestaurantExample() {

        viewModelScope.launch {
            restaurantFlow.onEach {
                println("Flow restaurant: $it is being delivered")
            }
                .collect {
                    println("Flow customer: $it is being eaten")
                    delay(5000)
                    println("Flow customer: finished eating $it")
                }
        }

        // output

        /*
        Flow restaurant : Appetizer is being delivered
        Flow customer : Appetizer is being eaten
        Flow customer : finished eating Appetizer
        Flow restaurant : Main dish is being delivered
        Flow customer : Main dish is being eaten
        Flow customer : finished eating Main dish
        Flow restaurant : dessert is being delivered
        Flow customer : dessert is being eaten
        Flow customer : finished eating dessert*/

        // this means that the flow waits for the consumer to finish to emit the new value

    }

    private fun flowRestaurantExample2() {

        viewModelScope.launch {
            restaurantFlow.onEach {
                println("Flow restaurant: $it is being delivered")
            }.buffer()
                .collect {
                    println("Flow customer: $it is being eaten")
                    delay(5000)
                    println("Flow customer: finished eating $it")
                }
        }

        // output with buffer
/*      Flow restaurant: Appetizer is being delivered
        Flow customer: Appetizer is being eaten
        Flow restaurant: Main dish is being delivered
        Flow restaurant: dessert is being delivered
        Flow customer: finished eating Appetizer
        Flow customer: Main dish is being eaten
        Flow customer: finished eating Main dish
        Flow customer: dessert is being eaten
        Flow customer: finished eating dessert*/

        // this means that the flow continues emitting without concern for the consumer


        // output with conflate
/*      Flow restaurant: Appetizer is being delivered
        Flow customer: Appetizer is being eaten
        Flow restaurant: Main dish is being delivered
        Flow restaurant: dessert is being delivered
        Flow customer: finished eating Appetizer
        Flow customer: dessert is being eaten
        Flow customer: finished eating dessert*/

        // this means that the coroutine continues with the latest emission but without quitting the task as collectLatest

        // just like this in collect latest

        /* Flow restaurant: Appetizer is being delivered
           Flow customer: Appetizer is being eaten
           Flow restaurant: Main dish is being delivered
           Flow customer: Main dish is being eaten
           Flow restaurant: dessert is being delivered
           Flow customer: dessert is being eaten
           Flow customer: finished eating dessert*/

    }

    // just for test, don't use it this way on real app for UI, for viewModel process may be ok
    private fun collectFlow() {
        viewModelScope.launch {
            // this collect gets called every time the flow emits something
            // retrieves all the values even if it is delayed
            countDownFlow
                .filter { time ->
                    time % 2 == 0
                }.map { time ->
                    time * time
                }
                .onEach { time ->
                    println("$time")
                }
                .collect { time ->
                    println("$time on collect")
                }
        }

        // also manages the flow in the coroutine scope
        countDownFlow.onEach {
            println(it)
        }.launchIn(viewModelScope)
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

    private fun flowOperators() {
        viewModelScope.launch {
            // this waits until the flow finishes all its values and returns the count of the matched logic

            // these are called terminator operators

            val count = countDownFlow
                .filter { time ->
                    time % 2 == 0
                }.map { time ->
                    time * time
                }
                .onEach { time ->
                    println("$time")
                }
                .count { time ->
                    time % 2 == 0
                }

            // we can also use flat maps and that kind of operators to flows
        }

        // also manages the flow in the coroutine scope
        countDownFlow.onEach {
            println(it)
        }.launchIn(viewModelScope)
    }


}