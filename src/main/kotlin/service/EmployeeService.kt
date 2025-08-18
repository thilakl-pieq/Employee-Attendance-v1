package service

import dao.Employee
import dao.EmployeeList
import dao.Role
import jakarta.ws.rs.core.Response
import model.LoginRequest
import org.slf4j.LoggerFactory

class EmployeeService(
    private val employeeList: EmployeeList
) {

    private val log = LoggerFactory.getLogger(EmployeeService::class.java)

    fun addEmployee(
        firstName: String,
        lastName: String,
        role: Role,
        department: String,
        reportingTo: String?
    ): Pair<Response.Status, Any> {

        log.info("Attempting to add employee: firstName=$firstName, lastName=$lastName, role=$role, department=$department, reportingTo=$reportingTo")

        val employee = Employee(firstName, lastName, role, department, reportingTo)

        if (!employeeList.add(employee)) {
            log.warn("Add employee failed: Employee with id=${employee.id} already exists")
            return Response.Status.CONFLICT to mapOf("error" to "Employee already exists")
        }

        log.info("Employee added successfully with id=${employee.id}")
        return Response.Status.CREATED to employee
    }

    fun getEmployee(id: String): Pair<Response.Status, Any> {
        log.debug("Fetching employee with id=$id")
        if (id.isBlank()) {
            log.warn("Get employee failed: blank ID provided")
            return Response.Status.BAD_REQUEST to mapOf("error" to "Employee ID cannot be empty")
        }
        val employee = employeeList.find { it.id == id }
            ?: run {
                log.warn("Get employee failed: No employee found for ID $id")
                return Response.Status.NOT_FOUND to mapOf("error" to "Employee not found")
            }

        log.debug("Employee found: $employee")
        return Response.Status.OK to employee
    }

    fun getAllEmployees(limit: Int = 20): List<Employee> {
        log.debug("Fetching up to $limit employees from list (total=${employeeList.size})")
        return employeeList.take(limit)
    }

    fun deleteEmployee(id: String): Pair<Response.Status, Any> {
        log.info("Attempting to delete employee with id=$id")
        if (id.isBlank()) {
            log.warn("Delete failed: blank ID provided")
            return Response.Status.BAD_REQUEST to mapOf("error" to "Employee ID cannot be empty")
        }
        val removed = employeeList.removeIf { it.id == id }
        return if (removed) {
            log.info("Employee with id=$id deleted successfully")
            Response.Status.OK to mapOf("message" to "Employee deleted successfully")
        } else {
            log.warn("Delete failed: Employee with id=$id not found")
            Response.Status.NOT_FOUND to mapOf("error" to "Employee not found")
        }
    }

    fun employeeExists(id: String): Boolean {
        val exists = employeeList.employeeExists(id)
        log.debug("Employee existence check for id=$id: $exists")
        return exists
    }
    fun login(employeeId: String, password: String): Pair<Response.Status, Any> {
        val trimmedId = employeeId.trim()
        val trimmedPassword = password.trim()

        log.info("Login attempt for employeeId=$trimmedId")

        if (trimmedId.isBlank() || trimmedPassword.isBlank()) {
            log.warn("Login failed due to blank employeeId or password")
            return Response.Status.BAD_REQUEST to mapOf("error" to "Employee ID and password cannot be empty")
        }

        val employee = employeeList.find { it.id == trimmedId }
            ?: run {
                log.warn("Login failed: Employee $trimmedId not found")
                return Response.Status.NOT_FOUND to mapOf("error" to "Employee not found")
            }

        // MOCK password validation (replace with real check)
        val isPasswordValid = (trimmedPassword == "password123")

        if (!isPasswordValid) {
            log.warn("Login failed: Invalid password for employee $trimmedId")
            return Response.Status.UNAUTHORIZED to mapOf("error" to "Invalid credentials")
        }

        log.info("Login successful for employee $trimmedId")
        return Response.Status.OK to employee
    }

}
