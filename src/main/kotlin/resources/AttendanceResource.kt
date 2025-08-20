package resources

import model.CheckInRequest
import model.CheckOutRequest
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import service.AttendanceService
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import java.util.UUID

@Path("/attendance")
class AttendanceResource(
    private val attendanceService: AttendanceService
) {

    private val log = LoggerFactory.getLogger(AttendanceResource::class.java)

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @POST
    @Path("/checkin/{id}")
    fun checkIn(@PathParam("id") id: String, request: CheckInRequest): Response {
        log.info("API /attendance/checkin called for $id")
        val uuid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid UUID format for employee ID"))
                .build()
        }
        val (status, body) = attendanceService.checkIn(uuid, request.checkInDateTime)
        return Response.status(status).entity(body).build()
    }

    @POST
    @Path("/checkout/{id}")
    fun checkOut(@PathParam("id") id: String, request: CheckOutRequest): Response {
        log.info("API /attendance/checkout called for $id")
        val uuid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid UUID format for employee ID"))
                .build()
        }
        val (status, body) = attendanceService.checkOut(uuid, request.checkOutDateTime)
        return Response.status(status).entity(body).build()
    }

    @GET
    @Path("/all")
    fun getAllAttendance(@QueryParam("limit") @DefaultValue("20") limit: Int): Response {
        log.debug("API /attendance/all called with limit=$limit")
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
            LocalDateTime.parse(from) // ISO format parsing
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid 'from' datetime format, expected ISO 8601 format like yyyy-MM-dd'T'HH:mm:ss"))
                .build()
        }

        val toDate: LocalDateTime = try {
            LocalDateTime.parse(to) // ISO format parsing
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Invalid 'to' datetime format, expected ISO 8601 format like yyyy-MM-dd'T'HH:mm:ss"))
                .build()
        }

        val (status, body) = attendanceService.getWorkingHoursSummary(fromDate, toDate)
        return Response.status(status).entity(body).build()
    }
}
