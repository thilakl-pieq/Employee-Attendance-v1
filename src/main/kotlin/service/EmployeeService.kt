package service

import dao.Employee
import dao.EmployeeDao
import dao.Role
import dao.Department
import jakarta.ws.rs.core.Response
import org.slf4j.LoggerFactory
import java.util.UUID

class EmployeeService(
    private val employeeDao: EmployeeDao
) {
    private val log = LoggerFactory.getLogger(EmployeeService::class.java)

    fun addEmployee(
        firstName: String,
        lastName: String,
        role: Role,
        department: Department,
        reportingTo: String?  // nullable string for reportingTo
    ): Pair<Response.Status, Any> {
        val emp = Employee(
            firstName = firstName,
            lastName = lastName,
            roleId = role.id,
            departmentId = department.id,
            reportingTo = reportingTo
        ) // ID auto-generated inside Employee class

        return try {
            employeeDao.insert(emp)
            log.info("Employee added successfully id=${emp.employeeId}")
            Response.Status.CREATED to emp
        } catch (e: Exception) {
            log.error("Error inserting employee", e)
            Response.Status.CONFLICT to mapOf("error" to (e.message ?: "Unknown error"))
        }
    }

    fun getEmployee(id: UUID): Pair<Response.Status, Any> {
        val emp = employeeDao.getById(id)
        return if (emp != null) {
            Response.Status.OK to emp
        } else {
            Response.Status.NOT_FOUND to mapOf("error" to "Employee not found")
        }
    }

    fun getAllEmployees(limit: Int = 20): List<Employee> = employeeDao.getAll(limit)

    fun deleteEmployee(id: UUID): Pair<Response.Status, Any> {
        val deleted = employeeDao.delete(id)
        return if (deleted > 0) {
            Response.Status.OK to mapOf("message" to "Employee deleted successfully")
        } else {
            Response.Status.NOT_FOUND to mapOf("error" to "Employee not found")
        }
    }

    fun login(employeeId: UUID, password: String): Pair<Response.Status, Any> {
        val emp = employeeDao.getById(employeeId)
        return if (emp == null) {
            Response.Status.NOT_FOUND to mapOf("error" to "Employee not found")
        } else if (password != "password123") { // Replace with actual authentication
            Response.Status.UNAUTHORIZED to mapOf("error" to "Invalid credentials")
        } else {
            Response.Status.OK to emp
        }
    }
}
