package model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class EmployeeRequest(

    @get:NotBlank(message = "First name is required")
    val firstname: String,

    @get:NotBlank(message = "Last name is required")
    val lastname: String,

    @get:NotBlank(message = "Role is required")
    val role: String,  // Will be mapped to Role enum

    @get:NotBlank(message = "Department is required")
    val department: String,  // Will be mapped to Department enum

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