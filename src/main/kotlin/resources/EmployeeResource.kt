package resources

import model.EmployeeRequest
import dao.Role
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import model.LoginRequest
import service.EmployeeService
import org.slf4j.LoggerFactory

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class EmployeeResource(private val employeeService: EmployeeService) {

    private val log = LoggerFactory.getLogger(EmployeeResource::class.java)

    @POST
    fun addEmployee(@Valid request: EmployeeRequest): Response {
        log.info("API /employees POST called with request: $request")
        val roleEnum = try {
            Role.valueOf(request.role.trim().uppercase())
        } catch (e: Exception) {
            log.warn("Invalid role '${request.role}' provided in request")
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid role value")).build()
        }

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
        log.info("API /employees/$id GET called")
        val (status, body) = employeeService.getEmployee(id)
        return Response.status(status).entity(body).build()
    }

    @GET
    fun listEmployees(@QueryParam("limit") @DefaultValue("20") limit: Int): Response {
        log.info("API /employees GET called with limit=$limit")
        val employees = employeeService.getAllEmployees(limit)
        return Response.ok(employees).build()
    }

    @DELETE
    @Path("/{id}")
    fun deleteEmployee(@PathParam("id") id: String): Response {
        log.info("API /employees/$id DELETE called")
        val (status, body) = employeeService.deleteEmployee(id)
        return Response.status(status).entity(body).build()
    }

    @POST
    @Path("/login")
    fun login(@Valid request: LoginRequest): Response {
        log.info("API /employees/login called for employeeId=${request.employeeId}")

        val (status, body) = employeeService.login(
            employeeId = request.employeeId,
            password = request.password
        )

        return Response.status(status).entity(body).build()
    }

}
