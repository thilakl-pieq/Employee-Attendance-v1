package dao

import org.jdbi.v3.core.Jdbi
import java.util.UUID

class EmployeeDao(private val jdbi: Jdbi) {

    fun insertEmployee(emp: Employee): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate(
                """
                INSERT INTO employee (employee_id, first_name, last_name, role_id, department_id, reporting_to)
                VALUES (:employeeId, :firstName, :lastName, :roleId, :departmentId, :reportingTo)
                """
            )
                .bindBean(emp)
                .execute()
        }
    }

    fun getById(id: UUID): Employee? {
        return jdbi.withHandle<Employee?, Exception> { handle ->
            handle.createQuery(
                """
                SELECT employee_id AS "employeeId",
                       first_name AS "firstName",
                       last_name AS "lastName",
                       role_id AS "roleId",
                       department_id AS "departmentId",
                       reporting_to AS "reportingTo"
                FROM employee
                WHERE employee_id = :id
                """
            )
                .bind("id", id)
                .mapTo(Employee::class.java)
                .findOne()
                .orElse(null)
        }
    }

    fun getAll(limit: Int = 20): List<Employee> {
        return jdbi.withHandle<List<Employee>, Exception> { handle ->
            handle.createQuery(
                """
                SELECT employee_id AS "employeeId",
                       first_name AS "firstName",
                       last_name AS "lastName",
                       role_id AS "roleId",
                       department_id AS "departmentId",
                       reporting_to AS "reportingTo"
                FROM employee
                LIMIT :limit
                """
            )
                .bind("limit", limit)
                .mapTo(Employee::class.java)
                .list()
        }
    }

    fun delete(id: UUID): Int {
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM employee WHERE employee_id = :id")
                .bind("id", id)
                .execute()
        }
    }

    // Get role ID by role name
    fun getRoleIdByName(roleName: String): Int? {
        return jdbi.withHandle<Int?, Exception> { handle ->
            handle.createQuery("SELECT role_id FROM roles WHERE LOWER(role_value) = LOWER(:roleName)")
                .bind("roleName", roleName)
                .mapTo(Int::class.java)
                .findOne()
                .orElse(null)
        }
    }

    // Get department ID by department name
    fun getDepartmentIdByName(deptName: String): Int? {
        return jdbi.withHandle<Int?, Exception> { handle ->
            handle.createQuery("SELECT department_id FROM departments WHERE LOWER(department_name) = LOWER(:deptName)")
                .bind("deptName", deptName)
                .mapTo(Int::class.java)
                .findOne()
                .orElse(null)
        }
    }
}
