package net.minikloon.fsmgasm

import java.time.Duration

fun main(args: Array<String>) {
    val series = StateSeries(listOf<State>(
            StateSeries(listOf<State>(
                    PrintState("Series 1, State 1"),
                    PrintState("Series 1, State 2")
            )),
            StateSeries(listOf<State>(
                    PrintState("Series 2, State 1"),
                    PrintState("Series 2, State 2"),
                    PrintState("Series 2, State 3")
            ))
    ))

    series.start()
    while(true) {
        series.update()
        Thread.sleep(10)
    }
}

class PrintState(val toPrint: String) : State() {
    override fun onStart() {
        println("Start: $toPrint")
    }

    override fun onUpdate() {
    }

    override fun onEnd() {
        println("End: $toPrint")
    }

    override val duration: Duration = Duration.ofSeconds(1)
}