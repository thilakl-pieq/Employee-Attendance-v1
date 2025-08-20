package dao

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class Employee(

    @JsonProperty("employee_id")
    val employeeId: UUID = UUID.randomUUID(),

    @get:NotBlank
    @get:Size(max = 100)
    @JsonProperty("first_name")
    val firstName: String,

    @get:NotBlank
    @get:Size(max = 100)
    @JsonProperty("last_name")
    val lastName: String,

    @JsonProperty("role_id")
    val roleId: Int,

    @JsonProperty("department_id")
    val departmentId: Int,

    @JsonProperty("reporting_to")
    val reportingTo: String? = null
) {
    override fun toString(): String {
        return "Employee(employeeId=$employeeId, firstName='$firstName', lastName='$lastName', roleId=$roleId, departmentId=$departmentId, reportingTo=$reportingTo)"
    }
}
