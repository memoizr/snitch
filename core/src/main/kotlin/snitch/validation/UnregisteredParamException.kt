package snitch.validation

import snitch.parameters.Parameter

data class UnregisteredParamException(val param: Parameter<*, *>) : Exception()