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

    fun getEmployee(id: UUID): Employee {
        return employeeDao.getById(id) ?: throw NotFoundException("Employee not found")
    }

    fun getAllEmployees(limit: Int = 20): List<Employee> = employeeDao.getAll(limit)

    fun deleteEmployee(id: UUID) {
        val deleted = employeeDao.delete(id)
        if (deleted == 0) {
            throw NotFoundException("Employee not found")
        }
        log.info("Employee deleted successfully id=$id")
    }

    fun login(employeeId: UUID, password: String): Employee {
        val emp = employeeDao.getById(employeeId) ?: throw NotFoundException("Employee not found")
        if (password != "password123") {
            throw BadRequestException("Invalid credentials")
        }
        return emp
    }
}
