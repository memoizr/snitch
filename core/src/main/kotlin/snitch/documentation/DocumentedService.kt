package snitch.documentation

import snitch.service.RoutedService

data class DocumentedService(val service: RoutedService, val documentation: Spec)