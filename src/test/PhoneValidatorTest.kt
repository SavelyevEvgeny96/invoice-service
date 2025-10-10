
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.sogaz.site.orderingService.validation.constraint.PhoneValidator

class PhoneValidatorTest {

    private lateinit var validator: PhoneValidator

    @BeforeEach
    fun setup() {
        val phoneRegex = Regex("^\\+?\\d[\\d ]*$")
        validator = PhoneValidator(phoneRegex)
    }

    @Test
    fun `should return true for valid phone numbers`() {
        val validPhones = listOf(
            "+71234567890",
            "89991234567",
            "1234567890",
            "123 456 7890"
        )

        validPhones.forEach { phone ->
            assertTrue(validator.isValid(phone, null), "Expected '$phone' to be valid")
        }
    }

    @Test
    fun `should return false for invalid phone numbers`() {
        val invalidPhones = listOf(
            "++123456",
            "+7(123)456-7890",
            "123-456-7890",
            "phone123",
            "+ 1234567890",
            "+7 12 34abc"
        )

        invalidPhones.forEach { phone ->
            assertFalse(validator.isValid(phone, null), "Expected '$phone' to be invalid")
        }
    }

    @Test
    fun `should return true for null or blank`() {
        assertTrue(validator.isValid(null, null))
        assertTrue(validator.isValid("", null))
        assertTrue(validator.isValid("   ", null))
    }
}