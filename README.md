# ![FSMgasm logo](http://i.imgur.com/hA3h42o.png) FSMgasm
[![](https://img.shields.io/badge/Kotlin-1.1.1-blue.svg)](https://kotlinlang.org/)
[![](https://jitpack.io/v/Minikloon/FSMgasm.svg)](https://jitpack.io/#Minikloon/FSMgasm)

FSMgasm is a Kotlin [state machine](http://www.skorks.com/2011/09/why-developers-never-use-state-machines/) library.
It is useful to model complex systems, simplify code and facilitate code reuse. 
The library is available under [MIT License](https://tldrlegal.com/license/mit-license).

# Install

FSMgasm is available on Maven through Jitpack.

Maven:
```xml
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>
<dependency>
  <groupId>com.github.Minikloon</groupId>
  <artifactId>FSMgasm</artifactId>
  <version>5c7311ce64</version>
</dependency>
```

Gradle:
```groovy
repositories {
  ...
  maven { url 'https://jitpack.io' }
}
dependencies {
  compile 'com.github.Minikloon:FSMgasm:-SNAPSHOT'
}
```

# Usage

Using FSMgasm is about creating states and composing them together.
A state is simple: it's something with a start, a duration and an end.
Sometimes a state will also do stuff in-between.

### Creating states

To create a state, override State:
```kotlin
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
```

Keep in mind **FSMgasm doesn't handle state execution for you**. 
This means there is no black magic behind using your newly-created state.

Using your state:
```kotlin
fun main(args: Array<String>) {
    val state = PrintState("Hello world!")
    state.start()
    state.end()
}
```

State does guarantee that `onStart()` and `onEnd()` will only be called once and that only a single onUpdate will be executed at a time.
It also checks that `start()` has been called before continuing execution of `update()` and `end()`.
These guarantees are retained in a multithreaded environment.

### Composing states

There are two classes to help you compose states together.

#### StateSeries

StateSeries lets you compose your states sequentially. It is typical to use a state series as the "main state" of a system.

```kotlin
fun main(args: Array<String>) {
    val series = StateSeries(
        PrintState("State 1"),
        PrintState("State 2")
    )
    
    series.start()
    while(true) {
        series.update()
    }
}
```

StateSeries will take care of checking whether the current state is over and switch to the next state in its update method.
Typically a state is over when it lasted for more than its duration. Duration is included in State because of how common it is. 
If your state doesn't need duration, you can override `State::isReadyToEnd` to setup your own ending condition.

You can setup a StateSeries either using the vararg constructor, a list of states, or adding them manually after construction using `StateSeries::add`.
`add` will add a state to the end of the series and can be used after initialization. `addNext` can be used to add a state right after the current state.

What makes state composition with FSMgasm is that **StateSeries extends State**. This means you can do something like:
```kotlin
fun main(args: Array<String>) {
    val series = StateSeries(
            StateSeries(
                    PrintState("Sub-Series 1, State 1"),
                    PrintState("Sub-Series 1, State 2")
            ),
            StateSeries(
                    PrintState("Sub-Series 2, State 1"),
                    PrintState("Sub-Series 2, State 2"),
                    PrintState("Sub-Series 2, State 3")
            )
    )
    
    series.start()
    while(true) {
        series.update()
        Thread.sleep(10)
        if(series.ended)
            break
    }
}
```

Another feature of State (and thus StateSeries) is the `frozen` property. 
```kotlin
series.frozen = true
```
This prevents State from ending and in the case of StateSeries, stops it from moving to the next state.
Freezing a state series can be useful when testing and debugging.

#### StateGroup

StateGroup lets you compose your states concurrently. *This doesn't mean they'll be executed on different threads*.
All the states within a StateGroup will be started on `StateGroup::start`, similarly with `end`.

```kotlin
fun main(args: Array<String>) {
    val group = StateGroup(
            PrintState("Hello"),
            PrintState("World!")
    )
    
    group.start()
    group.end()
}
```
StateGroup also extends State.

#### StateProxy

In some cases, you can't know all the states which are going to be needed at initialization ahead of time in a StateSeries.

For example, when modeling [Build Battle](https://www.youtube.com/watch?v=PXM5Xgjkhwo), the game starts with 12 players 
all building at the same time for 5 minutes. After the build time, players are teleported to each build for 30 seconds one 
at a time for judging. Builds of players who left aren't available for judging. This situation can modeled like so:
~~~~
StateSeries:
    1. StateGroup(12 x BuildState)
    2. PlayerCheckStateProxy => Creates 1 VoteState for each player still in the game
    3. AnnounceWinnerState
~~~~

A StateProxy may be implemented like this:
```kotlin
class TwelveYearsAState(series: StateSeries) : StateProxy(series) {
    override fun createStates(): List<State> {
        return (1..12).map { 
            PrintState("Proxied State $it")
        }.toList()
    }
}
```
Or with the shortcut method:
```kotlin
val series = StateSeries()
series.add(stateProxy(series) {
    (1..12).map {
        PrintState("Proxied State $it")
    }.toList()
})
```

#### StateSwitch

Not all situations can be easily modeled using a StateSeries, for example a game's menus. The player's navigation through the menus 
could go as such:
~~~~
MainMenuState => OptionMenuState => MainMenuState => StartGameState.
~~~~

This is where StateSwitch comes into play. It's a simple class which can be used as such:
```kotlin
val switch = StateSwitch()
    switch.changeState(PrintState("First!"))
    switch.changeState(PrintState("Second!"))
```

`StateSwitch::update` is provided as a convenience method to update the underlying state.
