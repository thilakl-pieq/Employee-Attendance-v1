package resources

import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import model.EmployeeRequest
import model.LoginRequest
import service.EmployeeService
import org.slf4j.LoggerFactory
import java.util.UUID

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class EmployeeResource(private val employeeService: EmployeeService) {

    private val log = LoggerFactory.getLogger(EmployeeResource::class.java)

    @POST
    fun addEmployee(@Valid request: EmployeeRequest): Response {
        log.info("POST /employees called with request: $request")
        log.info("${request.reportingto}")
        // No enums here, just pass strings as-is
        val reportingToId = request.reportingto?.takeIf { it.isNotBlank() }
        val (status, body) = employeeService.addEmployee(
            firstName = request.firstname.trim(),
            lastName = request.lastname.trim(),
            role = request.role.trim(),
            department = request.department.trim(),
            reportingTo = reportingToId
        )

        return Response.status(status).entity(body).build()
    }

    @GET
    @Path("/{id}")
    fun getEmployeeById(@PathParam("id") id: String): Response {
        val uuid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid UUID format for employee ID"))
                .build()
        }
        val (status, body) = employeeService.getEmployee(uuid)
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
        val uuid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid UUID format for employee ID"))
                .build()
        }
        val (status, body) = employeeService.deleteEmployee(uuid)
        return Response.status(status).entity(body).build()
    }

    @POST
    @Path("/login")
    fun login(@Valid request: LoginRequest): Response {
        val employeeUUID = try {
            UUID.fromString(request.employeeId)
        } catch (e: IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid UUID format for employeeId"))
                .build()
        }
        val (status, body) = employeeService.login(
            employeeId = employeeUUID,
            password = request.password
        )
        return Response.status(status).entity(body).build()
    }
}
