package resources

import api.EmployeeRequest
import dao.EmployeeList
import dao.Employee
import dao.Role
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class EmployeeResource(private val employeeList: EmployeeList) {

    @POST
    fun addEmployee(request: EmployeeRequest): Response {
        val roleEnum = try {
            Role.valueOf(request.role.trim().uppercase())
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid role value")).build()
        }

        val emp = Employee(
            firstName = request.firstname.trim(),
            lastName = request.lastname.trim(),
            role = roleEnum,
            department = request.department.trim(),
            reportingTo = request.reportingto?.trim()
        )

        if (!emp.isValid()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Employee validation failed")).build()
        }

//        if (employeeList.any { it.id == emp.id }) {
//            return Response.status(Response.Status.CONFLICT)
//                .entity(mapOf("error" to "Employee with this ID already exists")).build()
//        }

        employeeList.add(emp)
        return Response.status(Response.Status.CREATED).entity(emp).build()
    }
    @GET
    fun listEmployees(): List<Employee> = employeeList

}
