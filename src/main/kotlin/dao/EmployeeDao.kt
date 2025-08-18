package dao

import org.slf4j.LoggerFactory

class EmployeeList : ArrayList<Employee>() {

    private val log = LoggerFactory.getLogger(EmployeeList::class.java)

    override fun add(emp: Employee): Boolean {
        if (this.any { it.id == emp.id }) {
            log.warn("Attempt to add duplicate employee: id=${emp.id}")
            return false
        }
        log.debug("Adding employee: $emp")
        return super.add(emp)
    }

    fun employeeExists(id: String): Boolean {
        val exists = this.any { it.id == id }
        log.debug("Employee exists check for $id: $exists")
        return exists
    }

    fun getById(id: String): Employee? {
        log.debug("Searching for employee with id=$id")
        return this.find { it.id == id }
    }

    override fun toString(): String {
        if (this.isEmpty()) {
            log.debug("Employee list is empty")
            return "No employees found."
        }
        return this.joinToString("\n") { it.toString() }
    }
}
enum class Role {
    CEO,
    MANAGER,
    DEVELOPER
}