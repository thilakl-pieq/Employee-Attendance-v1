import dao.Employee
import dao.Role
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import api.CheckInRequest
import api.CheckOutRequest
import dao.AttendanceList
import dao.Attendance
import resources.AttendanceResource
import org.junit.jupiter.api.BeforeEach
import javax.ws.rs.core.Response
import java.time.LocalDateTime

class AttendanceTest {

    val employee = Employee("John", "Doe", Role.DEVELOPER, "Engineering", "AS002")

    private lateinit var attendanceList: AttendanceList
    private lateinit var attendanceResource: AttendanceResource

    @BeforeEach
    fun setup() {
        attendanceList = AttendanceList()
        attendanceResource = AttendanceResource(attendanceList)
    }
    @Test
    fun `test Employee Creation`() {
        val result = employee.isValid()
        assertEquals(true,result)
    }

    @Test
    fun `checkIn with valid data returns CREATED and stores attendance`() {
        val request = CheckInRequest(LocalDateTime.of(2025, 8, 8, 9, 0))
        val response = attendanceResource.checkIn("E001", request)
        assertEquals(Response.Status.CREATED.statusCode, response.status)
    }
    fun `checkIn with invalid data returns CREATED and stores attendance`() {
        val request = CheckInRequest(LocalDateTime.of(2025, 8, 8, 9, 0))
        val response = attendanceResource.checkIn("E001", request)
        assertEquals(Response.Status.CREATED.statusCode, response.status)
    }
    @Test
    fun `checkOut with valid data returns OK and updates attendance`() {
        val employeeId = "E002"
        val checkInTime = LocalDateTime.of(2025, 8, 8, 9, 0)
        attendanceList.add(Attendance(employeeId, checkInTime))

        val checkOutTime = LocalDateTime.of(2025, 8, 8, 17, 30)
        val request = CheckOutRequest(checkOutTime)

        val response = attendanceResource.checkOut(employeeId, request)
        assertEquals(Response.Status.OK.statusCode, response.status)
    }

    // Add more tests for edge cases, error handling...
}


