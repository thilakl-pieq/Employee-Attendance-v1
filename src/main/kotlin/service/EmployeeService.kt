package service

import dao.Employee
import dao.EmployeeList
import dao.Role
import jakarta.ws.rs.core.Response

class EmployeeService(
    private val employeeList: EmployeeList
) {

    fun addEmployee(
        firstName: String,
        lastName: String,
        role: Role,
        department: String,
        reportingTo: String?
    ): Pair<Response.Status, Any> {

        // Validation: All required fields
        if (firstName.isBlank() || lastName.isBlank() || department.isBlank()) {
            return Response.Status.BAD_REQUEST to mapOf("error" to "First name, last name and department cannot be empty")
        }

        // Build Employee object
        val employee = Employee(firstName, lastName, role, department, reportingTo)

        // Validate with Employee.isValid()
        if (!employee.isValid()) {
            return Response.Status.BAD_REQUEST to mapOf("error" to "Invalid employee data")
        }

        // Try to add to employee list
        if (!employeeList.add(employee)) {
            return Response.Status.CONFLICT to mapOf("error" to "Employee already exists or data is invalid")
        }

        return Response.Status.CREATED to employee
    }

    fun getEmployee(id: String): Pair<Response.Status, Any> {
        if (id.isBlank()) {
            return Response.Status.BAD_REQUEST to mapOf("error" to "Employee ID cannot be empty")
        }
        val employee = employeeList.find { it.id == id }
            ?: return Response.Status.NOT_FOUND to mapOf("error" to "Employee not found")
        return Response.Status.OK to employee
    }

    fun getAllEmployees(limit: Int = 20): List<Employee> {
        return employeeList.take(limit)
    }

    fun deleteEmployee(id: String): Pair<Response.Status, Any> {
        if (id.isBlank()) {
            return Response.Status.BAD_REQUEST to mapOf("error" to "Employee ID cannot be empty")
        }
        val removed = employeeList.removeIf { it.id == id }
        return if (removed) {
            Response.Status.OK to mapOf("message" to "Employee deleted successfully")
        } else {
            Response.Status.NOT_FOUND to mapOf("error" to "Employee not found")
        }
    }

    fun employeeExists(id: String): Boolean {
        return employeeList.employeeExists(id)
    }
}
