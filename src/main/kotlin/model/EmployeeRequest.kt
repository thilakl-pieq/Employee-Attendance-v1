package model

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class EmployeeRequest(
    val firstname: String,
    val lastname: String,
    val role: String,      // Will be converted to enum in resource
    val department: String,
    val reportingto: String?
)

data class LoginRequest(
    @get:NotBlank(message = "Employee ID cannot be blank")
    @JsonProperty("employeeId")
    val employeeId: String,

    @get:NotBlank(message = "Password cannot be blank")
    @JsonProperty("password")
    val password: String
)

