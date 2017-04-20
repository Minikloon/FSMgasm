package net.minikloon.fsmgasm

import net.minikloon.kloggs.logger
import java.time.Duration
import java.time.Instant

private val log = logger<State>()
abstract class State {
    var started: Boolean = false
        private set
    
    var ended: Boolean = false
        private set

    open var frozen: Boolean = false // prevents the state from ending
    
    private lateinit var startInstant: Instant
    private val lock = Any()
    
    fun start() {
        synchronized(lock) {
            if(started || ended)
                return
            started = true
        }
        
        startInstant = Instant.now()
        try {
            onStart()
        } catch(e: Throwable) {
            log.error(e) { "Exception during ${javaClass.name} start" }
        }
    }
    
    protected abstract fun onStart()
    
    private var updating = false
    fun update() {
        synchronized(lock) {
            if(!started || ended || updating)
                return
            updating = true
        }
        
        if(isReadyToEnd()) {
            end()
            return
        }
        
        try {
            onUpdate()
        } catch(e: Throwable) {
            log.error(e) { "Exception during ${javaClass.name} update" }
        }
        updating = false
    }
    
    abstract fun onUpdate()
    
    fun end() {
        synchronized(lock) {
            if(!started || ended)
                return
            ended = true
        }
        
        try {
            onEnd()
        } catch(e: Throwable) {
            log.error(e) { "Exception during ${javaClass.name} end" }
        }
    }

    open fun isReadyToEnd() : Boolean {
        return ended || remainingDuration == Duration.ZERO && !frozen
    }
    
    protected abstract fun onEnd()
    
    abstract val duration : Duration
    
    val remainingDuration: Duration
        get() {
            val sinceStart = Duration.between(startInstant, Instant.now())
            val remaining = duration - sinceStart
            return if(remaining.isNegative) Duration.ZERO else remaining
        }
}