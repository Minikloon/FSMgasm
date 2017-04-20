package net.minikloon.fsmgasm

open class StateSwitch {
    protected var state: State? = null
    
    fun changeState(next: State) {
        state?.end()
        state = next
        next.start()
    }
    
    fun update() {
        state?.update()
    }
}