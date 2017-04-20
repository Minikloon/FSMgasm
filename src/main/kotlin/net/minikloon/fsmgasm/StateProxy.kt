package net.minikloon.fsmgasm

import java.time.Duration

abstract class StateProxy(private val series: StateSeries) : State() {
    override fun onStart() {
        series.addNext(createStates())
    }
    
    abstract fun createStates() : List<State>

    override fun onUpdate() {}

    override fun onEnd() {}

    override val duration: Duration = Duration.ZERO
}

fun stateProxy(series: StateSeries, create: () -> List<State>) = LambdaStateProxy(series, create)
class LambdaStateProxy(series: StateSeries, val create: () -> List<State>) : StateProxy(series) {
    override fun createStates() = create()
}