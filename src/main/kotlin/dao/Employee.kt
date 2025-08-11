package dao

class Employee(
    var firstName: String,
    var lastName: String,
    var role: Role,
    var department: String,
    var reportingTo: String?
) {
    companion object {
        private var counter = 1
        fun generateId(firstName: String, lastName: String): String {
            val first = firstName.trim().firstOrNull()?.uppercaseChar() ?: 'X'
            val last = lastName.trim().lastOrNull()?.uppercaseChar() ?: 'Y'
            return "$first$last${String.format("%03d", counter++)}"
        }
    }

    val id: String = generateId(firstName, lastName)

    fun isValid(): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                department.isNotBlank() &&
                ((role == Role.CEO && reportingTo == null) ||
                        (role != Role.CEO && !reportingTo.isNullOrBlank()))
    }

    override fun toString(): String {
        return "ID: $id, Name: $firstName $lastName, Role: $role, Dept: $department, ReportsTo: ${reportingTo ?: "None"}"
    }
}
