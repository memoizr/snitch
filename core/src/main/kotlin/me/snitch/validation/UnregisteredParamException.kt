package me.snitch.validation

import me.snitch.parameters.Parameter

data class UnregisteredParamException(val param: Parameter<*, *>) : Exception()