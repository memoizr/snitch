package me.snitch.documentation

import me.snitch.service.RoutedService

data class DocumentedService(val service: RoutedService, val documentation: Spec)