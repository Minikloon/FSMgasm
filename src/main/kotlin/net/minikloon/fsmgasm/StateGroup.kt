package net.minikloon.fsmgasm

import java.time.Duration

open class StateGroup(states: List<State> = emptyList()) : StateHolder(states) {
    constructor(vararg states: State) : this(states.toList())

    override fun onStart() {
        states.forEach(State::start)
    }

    override fun onUpdate() {
        states.forEach(State::update)
        if(states.all { it.ended })
            end()
    }

    override fun onEnd() {
        states.forEach(State::end)
    }

    override fun isReadyToEnd() = states.all(State::isReadyToEnd)

    override val duration: Duration = states.maxBy { it.duration }?.duration ?: Duration.ZERO
}