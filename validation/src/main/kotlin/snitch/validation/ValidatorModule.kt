package snitch.validation

import snitch.shank.ShankModule
import snitch.shank.single


object ValidatorModule: ShankModule {
    val validator = single { -> HibernateDataClassValidator() }
}