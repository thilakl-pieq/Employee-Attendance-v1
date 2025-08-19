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

enum class Role(val id: Int) {
    CEO(1),
    MANAGER(2),
    DEVELOPER(3);

    companion object {
        fun fromId(id: Int): Role? = entries.find { it.id == id }
        fun fromName(name: String): Role? = entries.find { it.name.equals(name, ignoreCase = true) }
    }
}

enum class Department(val id: Int) {
    FINANCE(1),
    LEADERSHIP(2),
    IT(3),
    ENGINEERING(4);

    companion object {
        fun fromId(id: Int): Department? = entries.find { it.id == id }
        fun fromName(name: String): Department? = entries.find { it.name.equals(name, ignoreCase = true) }
    }
}
