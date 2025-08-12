package model

data class EmployeeRequest(
    val firstname: String,
    val lastname: String,
    val role: String,      // Will be converted to enum in resource
    val department: String,
    val reportingto: String?
)
