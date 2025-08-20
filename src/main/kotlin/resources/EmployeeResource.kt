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
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class EmployeeResource(
    private val employeeService: EmployeeService
) {

    private val log = LoggerFactory.getLogger(EmployeeResource::class.java)

    @POST
    fun addEmployee(@Valid request: EmployeeRequest): Response {
        log.info("POST /employees called with request: $request")
        val reportingToId = request.reportingto?.takeIf { it.isNotBlank() }
        return try {
            val emp = employeeService.addEmployee(
                firstName = request.firstname.trim(),
                lastName = request.lastname.trim(),
                role = request.role.trim(),
                department = request.department.trim(),
                reportingTo = reportingToId
            )
            Response.status(Response.Status.CREATED).entity(emp).build()
        } catch (e: BadRequestException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (ex: Exception) {
            log.error("Error during employee creation", ex)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Internal server error"))
                .build()
        }
    }

    @GET
    @Path("/{id}")
    fun getEmployeeById(@PathParam("id") id: String): Response {
        log.info("GET /employees/ID called with ID $id")
        val uuid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid UUID format for employee ID"))
                .build()
        }
        return try {
            val emp = employeeService.getEmployee(uuid)
            Response.ok(emp).build()
        } catch (e: NotFoundException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        }
    }

    @GET
    fun listEmployees(@QueryParam("limit") @DefaultValue("20") limit: Int): Response {
        log.info("GET /employees called with limit $limit")
        val employees = employeeService.getAllEmployees(limit)
        return Response.ok(employees).build()
    }

    @DELETE
    @Path("/{id}")
    fun deleteEmployee(@PathParam("id") id: String): Response {
        log.info("DELETE /employees/ID called with ID $id")
        val uuid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid UUID format for employee ID"))
                .build()
        }
        return try {
            employeeService.deleteEmployee(uuid)
            Response.ok(mapOf("message" to "Employee deleted successfully")).build()
        } catch (e: NotFoundException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        }
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
        return try {
            val emp = employeeService.login(employeeUUID, request.password)
            Response.ok(emp).build()
        } catch (e: NotFoundException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: BadRequestException) {
            Response.status(Response.Status.UNAUTHORIZED)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            log.error("Error during employee login", e)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Internal server error"))
                .build()
        }
    }
}
