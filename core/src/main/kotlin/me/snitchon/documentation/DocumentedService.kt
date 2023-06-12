package me.snitchon.documentation

import me.snitchon.service.RoutedService

data class DocumentedService(val service: RoutedService, val documentation: Spec)