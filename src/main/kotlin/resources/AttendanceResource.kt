package resources

import model.CheckInRequest
import model.CheckOutRequest
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.MediaType
import service.AttendanceService
import java.time.LocalDateTime
import java.util.UUID
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.slf4j.LoggerFactory

@Path("/attendance")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class AttendanceResource(
    private val attendanceService: AttendanceService
) {

    private val log = LoggerFactory.getLogger(AttendanceResource::class.java)

    @POST
    @Path("/checkin")
    fun checkIn(
        @QueryParam("employee_id") employeeId: String?,
        request: CheckInRequest
    ): Response {
        if (employeeId.isNullOrBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Query parameter 'employee_id' is required"))
                .build()
        }

        log.info("POST /attendance/checkIn called for employee")

        val uuid = try {
            UUID.fromString(employeeId)
        } catch (e: IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid UUID format for employee ID"))
                .build()
        }

        return try {
            val attendance = attendanceService.checkIn(uuid, request.checkInDateTime)
            Response.status(Response.Status.CREATED).entity(attendance).build()
        } catch (e: NotFoundException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: BadRequestException) {
            Response.status(Response.Status.CONFLICT)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            log.error("Error during check-in", e)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Internal server error"))
                .build()
        }
    }

    @POST
    @Path("/checkout")
    fun checkOut(
        @QueryParam("employee_id") employeeId: String?,
        request: CheckOutRequest
    ): Response {
        if (employeeId.isNullOrBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Query parameter 'employee_id' is required"))
                .build()
        }

        log.info("POST /attendance/checkout called for employee_id=$employeeId")

        val uuid = try {
            UUID.fromString(employeeId)
        } catch (e: IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid UUID format for employee ID"))
                .build()
        }

        return try {
            attendanceService.checkOut(uuid, request.checkOutDateTime)
            Response.ok(mapOf("message" to "Check-out successful")).build()
        } catch (e: NotFoundException) {
            Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: BadRequestException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            log.error("Error during check-out", e)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Internal server error"))
                .build()
        }
    }


    @GET
    fun getAllAttendance(@QueryParam("limit") @DefaultValue("20") limit: Int): Response {
        log.debug("GET /attendance/all called with limit=$limit")
        val list = attendanceService.getAllAttendances(limit)
        return Response.ok(list).build()
    }

    @GET
    @Path("/summary")
    fun getAttendanceSummary(
        @QueryParam("from") from: String?,
        @QueryParam("to") to: String?
    ): Response {
        log.info("API /attendance/summary called: from=$from, to=$to")

        if (from.isNullOrBlank() || to.isNullOrBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Both 'from' and 'to' query parameters are required in ISO 8601 format, e.g. yyyy-MM-dd'T'HH:mm:ss"))
                .build()
        }

        val fromDate: LocalDateTime = try {
            LocalDateTime.parse(from)
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid 'from' datetime format, expected ISO 8601 format like yyyy-MM-dd'T'HH:mm:ss"))
                .build()
        }

        val toDate: LocalDateTime = try {
            LocalDateTime.parse(to)
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid 'to' datetime format, expected ISO 8601 format like yyyy-MM-dd'T'HH:mm:ss"))
                .build()
        }

        return try {
            val summary = attendanceService.getWorkingHoursSummary(fromDate, toDate)
            Response.ok(summary).build()
        } catch (e: BadRequestException) {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to e.message))
                .build()
        } catch (e: Exception) {
            log.error("Error fetching attendance summary", e)
            Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(mapOf("error" to "Internal server error"))
                .build()
        }
    }
}
