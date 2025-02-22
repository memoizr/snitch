package snitch.validation

import jakarta.validation.constraints.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import snitch.types.ValidatedDataClass

class ValidationTest {
    private val validator = HibernateDataClassValidator()

    data class TestUser(
        @field:NotBlank
        val name: String,
        
        @field:Email
        val email: String,
        
        @field:Min(18)
        val age: Int,
        
        @field:Size(min = 8, max = 30)
        val password: String
    )

    @Test
    fun `should validate valid data class`() {
        val user = TestUser(
            name = "John Doe",
            email = "john@example.com",
            age = 25,
            password = "secure123"
        )

        val result = validator.validate(user)
        assertThat(result).isInstanceOf(ValidatedDataClass.Valid::class.java)
        assertThat((result as ValidatedDataClass.Valid<*>).data).isEqualTo(user)
    }

    @Test
    fun `should return validation errors for invalid data`() {
        val user = TestUser(
            name = "",  // invalid: blank
            email = "not-an-email",  // invalid: not email format
            age = 16,  // invalid: under 18
            password = "123"  // invalid: too short
        )

        val result = validator.validate(user)
        assertThat(result).isInstanceOf(ValidatedDataClass.Invalid::class.java)
        
        val errors = (result as ValidatedDataClass.Invalid).errors
        assertThat(errors).hasSize(4)
        
        val errorMessages = errors.map { it.error }
        assertThat(errorMessages).contains(
            "name: must not be blank",
            "email: must be a well-formed email address",
            "age: must be greater than or equal to 18",
            "password: size must be between 8 and 30"
        )
    }
} 