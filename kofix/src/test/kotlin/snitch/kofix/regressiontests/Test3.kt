package snitch.kofix.regressiontests

import com.memoizr.assertk.expect
import org.junit.Test
import snitch.kofix.aRandomListOf
import java.io.Serializable

sealed class SendOption : Serializable

object AddUsersToSendOption : SendOption()
object ShowAllUsersToSendOption : SendOption()
sealed class SendDestination<out DESTINATION> : SendOption() {
    abstract val destination: DESTINATION
    abstract val sendType: SendType
}

data class AppSendDestination(override val destination: SupportedApp, override val sendType: SendType) : SendDestination<SupportedApp>()
data class UserSendDestination(override val destination: SupportedUser, override val sendType: SendType) : SendDestination<SupportedUser>()

enum class SupportedApp {
    WHATSAPP,
    FACEBOOK_MESSENGER,
    INSTAGRAM,
    ALLO,
    KIK,
    FACEBOOK,
    TELEGRAM,
    SLACK,
    ANDROID_MESSAGES,
    HANGOUTS,
    TWITTER,
    SKYPE,
    GOOGLE_PLUS,
    INBOX,
    GMAIL
}

sealed class SupportedUser {
    abstract val name: String
    abstract val photo: Image
}

data class WhatsappUserSendDestination(
        override val name: String,
        override val photo: Image
) : SupportedUser()

enum class SendType { URL, FILE }

interface One
class Two: One
class Three: One
sealed class TheClass


class Test3 {
    private val sendOptions by aRandomListOf<SendOption>()

    @Test
    fun `it works`() {
        expect that sendOptions.all { it is SendOption } _is true
    }
}