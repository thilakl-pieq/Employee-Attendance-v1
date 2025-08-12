package resources

import model.CheckInRequest
import model.CheckOutRequest
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import service.AttendanceService
import java.time.LocalDateTime

@Path("/attendance")
class AttendanceResource(
    private val attendanceService: AttendanceService
) {
    @POST
    @Path("/checkin/{id}")
    fun checkIn(@PathParam("id") id: String, request: CheckInRequest): Response {
        val (status, body) = attendanceService.checkIn(id, request.checkInDateTime)
        return Response.status(status).entity(body).build()
    }

    @POST
    @Path("/checkout/{id}")
    fun checkOut(@PathParam("id") id: String, request: CheckOutRequest): Response {
        val (status, body) = attendanceService.checkOut(id, request.checkOutDateTime)
        return Response.status(status).entity(body).build()
    }

    @GET
    @Path("/all")
    fun getAllAttendance(@QueryParam("limit") @DefaultValue("20") limit: Int): Response {
        val list = attendanceService.getAllAttendances().take(limit)
        return Response.ok(list).build()
    }

    @GET
    @Path("/summary")
    fun getAllAttendance(@QueryParam("to") to: LocalDateTime,
                         @QueryParam("from") from:LocalDateTime?,
                         @QueryParam("limit") @DefaultValue("20") limit: Int): Response {



    }
}
