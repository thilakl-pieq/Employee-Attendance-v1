package dao

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.mapTo
import org.slf4j.LoggerFactory
import java.util.UUID

class EmployeeDao(private val jdbi: Jdbi) {

    private val log = LoggerFactory.getLogger(EmployeeDao::class.java)

    fun insertEmployee(emp: Employee): Int {
        log.info("Inserting employee record into db from dao layer")
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("""
                INSERT INTO employee (employee_id, first_name, last_name, role_id, department_id, reporting_to)
                VALUES (:employeeId, :firstName, :lastName, :roleId, :departmentId, :reportingTo)
            """)
                .bindBean(emp)
                .execute()
        }
    }

    fun getById(id: UUID): Employee? {
        log.info("Retrieving employee by id $id from dao layer")
        return jdbi.withHandle<Employee?, Exception> { handle ->
            handle.createQuery("""
                SELECT employee_id AS "employeeId",
                       first_name AS "firstName",
                       last_name AS "lastName",
                       role_id AS "roleId",
                       department_id AS "departmentId",
                       reporting_to AS "reportingTo"
                FROM employee
                WHERE employee_id = :id
            """)
                .bind("id", id)
                .mapTo<Employee>()
                .findOne()
                .orElse(null)
        }
    }

    fun getAll(limit: Int = 20): List<Employee> {
        log.info("Retrieving all employees with limit $limit from dao layer")
        return jdbi.withHandle<List<Employee>, Exception> { handle ->
            handle.createQuery("""
                SELECT employee_id AS "employeeId",
                       first_name AS "firstName",
                       last_name AS "lastName",
                       role_id AS "roleId",
                       department_id AS "departmentId",
                       reporting_to AS "reportingTo"
                FROM employee
                LIMIT :limit
            """)
                .bind("limit", limit)
                .mapTo<Employee>()
                .list()
        }
    }

    fun delete(id: UUID): Int {
        log.info("Deleting employee with id $id from dao layer")
        return jdbi.withHandle<Int, Exception> { handle ->
            handle.createUpdate("DELETE FROM employee WHERE employee_id = :id")
                .bind("id", id)
                .execute()
        }
    }

    fun getRoleIdByName(roleName: String): Int? {
        log.info("Retrieving role id for role name '$roleName'")
        return jdbi.withHandle<Int?, Exception> { handle ->
            handle.createQuery("SELECT role_id FROM roles WHERE LOWER(role_value) = LOWER(:roleName)")
                .bind("roleName", roleName)
                .mapTo<Int>()
                .findOne()
                .orElse(null)
        }
    }

    fun getDepartmentIdByName(deptName: String): Int? {
        log.info("Retrieving department id for department name '$deptName'")
        return jdbi.withHandle<Int?, Exception> { handle ->
            handle.createQuery("SELECT department_id FROM departments WHERE LOWER(department_name) = LOWER(:deptName)")
                .bind("deptName", deptName)
                .mapTo<Int>()
                .findOne()
                .orElse(null)
        }
    }
}
