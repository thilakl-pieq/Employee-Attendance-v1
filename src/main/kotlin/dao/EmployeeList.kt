package dao

class EmployeeList : ArrayList<Employee>() {

    override fun add(emp: Employee): Boolean {
        if (!emp.isValid()) return false
        if (this.any { it.id == emp.id }) {
            return false // Employee ID must be unique
        }
        return super.add(emp)
    }
//
//    fun deleteEmployee(id: String): Boolean {
//        return this.removeIf { it.id == id }
//    }
//
//    fun employeeExists(id: String): Boolean {
//        return this.any { it.id == id }
//    }

    override fun toString(): String {
        if (this.isEmpty()) return "No employees found."
        return this.joinToString("\n") { it.toString() }
    }
}
enum class Role {
    CEO,
    MANAGER,
    DEVELOPER
}

