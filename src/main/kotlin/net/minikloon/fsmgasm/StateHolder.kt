package net.minikloon.fsmgasm

abstract class StateHolder(states: List<State> = emptyList()) : State(), Iterable<State> {
    protected val states: MutableList<State> = states.toMutableList()
    
    fun add(state: State) {
        states.add(state)
    }
    
    fun addAll(newStates: Collection<State>) {
        states.addAll(newStates)
    }

    override var frozen: Boolean = false
        set(value) {
            states.forEach { it.frozen = value }
            field = value
        }

    override fun iterator() = states.iterator()
}