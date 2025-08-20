package service

import dao.Employee
import dao.EmployeeDao
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
        role: String,
        department: String,
        reportingTo: String? = null
    ): Pair<Response.Status, Any> {
        val roleId = employeeDao.getRoleIdByName(role.trim())
        if (roleId == null) {
            return Response.Status.BAD_REQUEST to mapOf("error" to "Invalid role '$role'")
        }
        val deptId = employeeDao.getDepartmentIdByName(department.trim())
        if (deptId == null) {
            return Response.Status.BAD_REQUEST to mapOf("error" to "Invalid department '$department'")
        }
        val emp = Employee(
            firstName = firstName,
            lastName = lastName,
            roleId = roleId,
            departmentId = deptId,
            reportingTo = reportingTo
        )
        return try {
            employeeDao.insertEmployee(emp)
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
        return when {
            emp == null -> Response.Status.NOT_FOUND to mapOf("error" to "Employee not found")
            password != "password123" -> Response.Status.UNAUTHORIZED to mapOf("error" to "Invalid credentials")
            else -> Response.Status.OK to emp
        }
    }
}
