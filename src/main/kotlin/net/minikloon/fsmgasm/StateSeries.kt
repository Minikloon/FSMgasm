package net.minikloon.fsmgasm

import java.time.Duration

open class StateSeries(states: List<State> = emptyList()) : StateHolder(states) {
    protected var current = 0
    protected var skipping: Boolean = false

    constructor(vararg states: State) : this(states.toList())

    fun addNext(state: State) {
        states.add(current + 1, state)
    }
    
    fun addNext(newStates: List<State>) {
        var i = 1
        newStates.forEach { state ->
            states.add(current + i, state)
            ++i
        }
    }
    
    fun skip() {
        skipping = true
    }
    
    override fun onStart() {
        if(states.isEmpty()) {
            end()
            return
        }
        
        states[current].start()
    }

    override fun onUpdate() {
        states[current].update()
        
        if(states[current].isReadyToEnd() || skipping) {
            if(skipping)
                skipping = false
            
            states[current].end()
            ++current
            
            if(current >= states.size) {
                end()
                return
            }
            
            states[current].start()
        }
    }

    override fun isReadyToEnd(): Boolean {
        return states[current].isReadyToEnd() && current >= states.size && !frozen
    }

    override fun onEnd() {
        if(current < states.size)
            states[current].end()
    }
    
    override val duration: Duration = states.fold(Duration.ZERO) { curr, state -> curr + state.duration }
}