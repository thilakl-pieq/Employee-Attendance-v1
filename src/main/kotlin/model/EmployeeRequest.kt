package model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class EmployeeRequest(

    @get:NotBlank(message = "First name is required")
    val firstname: String,

    @get:NotBlank(message = "Last name is required")
    val lastname: String,

    @get:NotBlank(message = "Role is required")
    val role: String,

    @get:NotBlank(message = "Department is required")
    val department: String,
    @get:NotBlank(message = "reporting to id is required")
    val reportingto: String? = null
)
data class LoginRequest(
    @get:NotBlank(message = "Employee ID cannot be blank")
    @JsonProperty("employeeId")
    val employeeId: String,

    @get:NotBlank(message = "Password cannot be blank")
    @JsonProperty("password")
    val password: String
)