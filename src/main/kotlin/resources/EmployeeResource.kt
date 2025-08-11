package resources

import api.EmployeeRequest
import dao.Role
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import service.EmployeeService

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class EmployeeResource(private val employeeService: EmployeeService) {

    @POST
    fun addEmployee(request: EmployeeRequest): Response {
        val roleEnum = try {
            Role.valueOf(request.role.trim().uppercase())
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid role value")).build()
        }

        // Delegate to the service layer
        val (status, body) = employeeService.addEmployee(
            firstName = request.firstname.trim(),
            lastName = request.lastname.trim(),
            role = roleEnum,
            department = request.department.trim(),
            reportingTo = request.reportingto?.trim()
        )

        return Response.status(status).entity(body).build()
    }

    @GET
    @Path("/{id}")
    fun getEmployeeById(@PathParam("id") id: String): Response {
        val (status, body) = employeeService.getEmployee(id)
        return Response.status(status).entity(body).build()
    }

    @GET
    fun listEmployees(@QueryParam("limit") @DefaultValue("20") limit: Int): Response {
        val employees = employeeService.getAllEmployees(limit)
        return Response.ok(employees).build()
    }

    @DELETE
    @Path("/{id}")
    fun deleteEmployee(@PathParam("id") id: String): Response {
        val (status, body) = employeeService.deleteEmployee(id)
        return Response.status(status).entity(body).build()
    }
}
