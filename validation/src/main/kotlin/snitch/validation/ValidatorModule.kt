package snitch.validation

import life.shank.ShankModule
import life.shank.single

object ValidatorModule: ShankModule {
    val validator = single { -> HibernateDataClassValidator() }
}