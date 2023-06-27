package me.snitchon.validation

import me.snitchon.parameters.Parameter

data class UnregisteredParamException(val param: Parameter<*, *>) : Exception()