package resources

import api.CheckInRequest
import api.CheckOutRequest
import dao.Attendance
import dao.AttendanceList
import javax.ws.rs.*
//import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/attendance")
//@Produces(MediaType.APPLICATION_JSON)
//@Consumes(MediaType.APPLICATION_JSON)
class AttendanceResource(private val attendanceList: AttendanceList) {

    @POST
    @Path("/checkin/{id}")
    fun checkIn(
        @PathParam("id") id: String,
        request: CheckInRequest
    ): Response {
        val employeeId = id.trim()
        if (employeeId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Employee ID cannot be left empty"))
                .build()
        }

        if (attendanceList.hasAlreadyCheckedIn(employeeId, request.checkInDateTime)) {
            return Response.status(Response.Status.CONFLICT)
                .entity(mapOf("error" to "Employee has already checked in on this date"))
                .build()
        }

        val attendance = Attendance(
            id = employeeId,
            checkInDateTime = request.checkInDateTime
        )

        val added = attendanceList.add(attendance)
        if (!added) {
            return Response.status(Response.Status.CONFLICT)
                .entity(mapOf("error" to "Employee already has an open attendance entry"))
                .build()
        }

        return Response.status(Response.Status.CREATED).entity(attendance).build()
    }

    @POST
    @Path("/checkout/{id}")
    fun checkOut(
        @PathParam("id") id: String,
        request: CheckOutRequest
    ): Response {
        val employeeId = id.trim()
        if (employeeId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to "Employee ID cannot be left empty"))
                .build()
        }

        val openAttendance = attendanceList.find { it.id == employeeId && it.checkOutDateTime == null }
            ?: return Response.status(Response.Status.NOT_FOUND)
                .entity(mapOf("error" to "No open attendance record found for employee ID: $employeeId"))
                .build()

        val (success, message) = attendanceList.addCheckOutToList(openAttendance, request.checkOutDateTime ?: return Response.status(Response.Status.BAD_REQUEST)
            .entity(mapOf("error" to "Check-out datetime must be provided"))
            .build())

        return if (success) {
            Response.ok(openAttendance).build()
        } else {
            Response.status(Response.Status.BAD_REQUEST)
                .entity(mapOf("error" to message))
                .build()
        }
    }

    @GET
    fun listAttendance(): List<Attendance> = attendanceList

}
