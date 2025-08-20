package service

import dao.Employee
import dao.EmployeeDao
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.slf4j.LoggerFactory
import java.util.UUID

class EmployeeService(
    private val employeeDao: EmployeeDao
) {
    private val log = LoggerFactory.getLogger(EmployeeService::class.java)

    /**
     * Adds a new employee after validating the role and department.
     * @throws BadRequestException if role or department is invalid
     * @throws Exception on insert failure
     */
    fun addEmployee(
        firstName: String,
        lastName: String,
        role: String,
        department: String,
        reportingTo: String? = null
    ): Employee {
        val roleId = employeeDao.getRoleIdByName(role.trim()) ?: throw BadRequestException("Invalid role '$role'")
        val deptId = employeeDao.getDepartmentIdByName(department.trim())
            ?: throw BadRequestException("Invalid department '$department'")

        val emp = Employee(
            firstName = firstName,
            lastName = lastName,
            roleId = roleId,
            departmentId = deptId,
            reportingTo = reportingTo
        )

        try {
            employeeDao.insertEmployee(emp)
            log.info("Employee added successfully id=${emp.employeeId}")
            return emp
        } catch (e: Exception) {
            log.error("Error inserting employee", e)
            throw e
        }
    }

    /**
     * Retrieves an employee by UUID.
     * @throws NotFoundException if employee not found
     */
    fun getEmployee(id: UUID): Employee {
        return employeeDao.getById(id) ?: throw NotFoundException("Employee not found")
    }

    /**
     * Returns a list of employees with optional limit.
     */
    fun getAllEmployees(limit: Int = 20): List<Employee> = employeeDao.getAll(limit)

    /**
     * Deletes an employee by UUID.
     * @throws NotFoundException if employee not found
     */
    fun deleteEmployee(id: UUID) {
        val deleted = employeeDao.delete(id)
        if (deleted == 0) {
            throw NotFoundException("Employee not found")
        }
        log.info("Employee deleted successfully id=$id")
    }

    /**
     * Simulates a login by checking employee existence and password.
     * @throws NotFoundException if employee not found
     * @throws BadRequestException if credentials are invalid
     */
    fun login(employeeId: UUID, password: String): Employee {
        val emp = employeeDao.getById(employeeId) ?: throw NotFoundException("Employee not found")
        if (password != "password123") {
            throw BadRequestException("Invalid credentials")
        }
        return emp
    }
}
