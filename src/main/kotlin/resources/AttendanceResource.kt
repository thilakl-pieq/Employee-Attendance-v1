package resources

import model.CheckInRequest
import model.CheckOutRequest
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import service.AttendanceService
import java.time.LocalDateTime
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

@Path("/attendance")
class AttendanceResource(
    private val attendanceService: AttendanceService
) {

    private val log = LoggerFactory.getLogger(AttendanceResource::class.java)

    @POST
    @Path("/checkin/{id}")
    fun checkIn(@PathParam("id") id: String, request: CheckInRequest): Response {
        log.info("API /attendance/checkin called for $id")
        val (status, body) = attendanceService.checkIn(id, request.checkInDateTime)
        return Response.status(status).entity(body).build()
    }

    @POST
    @Path("/checkout/{id}")
    fun checkOut(@PathParam("id") id: String, request: CheckOutRequest): Response {
        log.info("API /attendance/checkout called for $id")
        val (status, body) = attendanceService.checkOut(id, request.checkOutDateTime)
        return Response.status(status).entity(body).build()
    }

    @GET
    @Path("/all")
    fun getAllAttendance(@QueryParam("limit") @DefaultValue("20") limit: Int): Response {
        log.debug("API /attendance/all called with limit=$limit")
        val list = attendanceService.getAllAttendances().take(limit)
        return Response.ok(list).build()
    }

    @GET
    @Path("/summary")
    fun getSummary(
        @QueryParam("to") toStr: String?,
        @QueryParam("from") fromStr: String?
    ): Response {
        if (fromStr == null || toStr == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "`from` and `to` query params are required in format yyyy-MM-ddTHH:mm"))
                .build()
        }

        // Parse to LocalDateTime
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME // or a custom pattern if your format differs
        val from = try {
            LocalDateTime.parse(fromStr, formatter)
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "`from` query param is not a valid date-time"))
                .build()
        }
        val to = try {
            LocalDateTime.parse(toStr, formatter)
        } catch (e: Exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "`to` query param is not a valid date-time"))
                .build()
        }

        val (status, body) = attendanceService.getAttendanceBySummary(from, to)
        return Response.status(status).entity(body).build()
    }

}
