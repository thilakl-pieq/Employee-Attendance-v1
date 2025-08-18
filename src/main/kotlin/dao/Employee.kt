package dao

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class Employee(

    @get:NotBlank(message = "First name cannot be blank")
    @get:Size(max = 100, message = "First name must not exceed 100 characters")
    @JsonProperty("firstName")
    val firstName: String,

    @get:NotBlank(message = "Last name cannot be blank")
    @get:Size(max = 100, message = "Last name must not exceed 100 characters")
    @JsonProperty("lastName")
    val lastName: String,

    @get:NotBlank(message = "Role cannot be blank")
    @JsonProperty("role")
    val role: Role,

    @get:NotBlank(message = "Department cannot be blank")
    @get:Size(max = 100, message = "Department name must not exceed 100 characters")
    @JsonProperty("department")
    val department: String,

    @JsonProperty("reportingTo")
    val reportingTo: String? = null

) {
    @JsonProperty("id")
    val id: String = generateId(firstName, lastName)

    companion object {
        private var counter = 1
        fun generateId(firstName: String, lastName: String): String {
            val first = firstName.trim().firstOrNull()?.uppercaseChar() ?: 'X'
            val last = lastName.trim().lastOrNull()?.uppercaseChar() ?: 'Y'
            return "$first$last${String.format("%03d", counter++)}"
        }
    }
}
