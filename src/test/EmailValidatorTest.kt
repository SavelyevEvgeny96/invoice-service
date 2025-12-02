import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.sogaz.site.orderingService.validation.constraint.EmailValidator

class EmailValidatorTest {
    private lateinit var validator: EmailValidator

    @BeforeEach
    fun setup() {
        val emailRegex = Regex("^(?!\\.)(?!.*\\.\\.)[a-zA-Z0-9._%+-]+(?<!\\.)@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
        validator = EmailValidator(emailRegex)
    }

    @Test
    fun `should return true for valid emails`() {
        val validEmails =
            listOf(
                "user@example.com",
                "user.name+tag@domain.co",
                "user_name@domain.org",
                "u.ser@sub.domain.com",
            )

        validEmails.forEach { email ->
            assertTrue(validator.isValid(email, null), "Expected '$email' to be valid")
        }
    }

    @Test
    fun `should return false for invalid emails`() {
        val invalidEmails =
            listOf(
                "userexample.com",
                "user@.com",
                ".user@domain.com",
                "user@domain..com",
                "user@domain",
                "user@domain.c",
            )

        invalidEmails.forEach { email ->
            assertFalse(validator.isValid(email, null), "Expected '$email' to be invalid")
        }
    }

    @Test
    fun `should return true for null or blank`() {
        assertTrue(validator.isValid(null, null))
        assertTrue(validator.isValid("", null))
        assertTrue(validator.isValid("   ", null))
    }
}
