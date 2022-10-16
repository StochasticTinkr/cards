package com.stochastictinkr.animation

import java.time.Duration


data class Transition(val duration: Duration, val action: (Double) -> Unit)