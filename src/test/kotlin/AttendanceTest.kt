import dao.Employee
import dao.Role
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import model.CheckInRequest
import model.CheckOutRequest
import dao.AttendanceList
import dao.Attendance
import service.AttendanceService
import resources.AttendanceResource
import org.junit.jupiter.api.BeforeEach
import jakarta.ws.rs.core.Response
import java.time.LocalDateTime
import jakarta.ws.rs.client.Client
import jakarta.ws.rs.client.Invocation
import jakarta.ws.rs.client.WebTarget
import org.mockito.Mockito.*

class AttendanceTest {



    private lateinit var attendanceList: AttendanceList
    private lateinit var attendanceResource: AttendanceResource
    private lateinit var mockClient: Client

    @BeforeEach
    fun setup() {
        attendanceList = AttendanceList()

        // Mock HTTP client so employeeExists() always returns true
        mockClient = mock(Client::class.java)
        val mockTarget = mock(WebTarget::class.java)
        val mockRequest = mock(Invocation.Builder::class.java)
        val mockResponse = mock(jakarta.ws.rs.core.Response::class.java)

        `when`(mockClient.target(anyString())).thenReturn(mockTarget)
        `when`(mockTarget.request()).thenReturn(mockRequest)
        `when`(mockRequest.get()).thenReturn(mockResponse)
        `when`(mockResponse.status).thenReturn(200)

        // Build the service and resource
        val attendanceService = AttendanceService(attendanceList, mockClient)
        attendanceResource = AttendanceResource(attendanceService)
    }

//    @Test
//    fun `test Employee Creation`() {
//        val employee = Employee("John", "Doe", Role.DEVELOPER, "Engineering", "AS002")
//        val result = employee.isValid()
//        assertEquals(true, result)
//    }

    @Test
    fun `checkIn with valid data returns CREATED and stores attendance`() {
        val request = CheckInRequest(LocalDateTime.of(2025, 8, 8, 9, 0))
        val response = attendanceResource.checkIn("E001", request)
        assertEquals(Response.Status.CREATED.statusCode, response.status)
    }

    @Test
    fun `checkIn followed by checkout using invalid checkout time`() {
        val requestCheckin = CheckInRequest(LocalDateTime.of(2025, 8, 8, 9, 0))
        attendanceResource.checkIn("E001", requestCheckin)

        val checkOutTime = LocalDateTime.of(2025, 8, 7, 17, 30) // Before check-in
        val requestCheckout = CheckOutRequest(checkOutTime)
        val response = attendanceResource.checkOut("E001", requestCheckout)

        assertEquals(Response.Status.BAD_REQUEST.statusCode, response.status)

        val entity = response.entity as Map<*, *>
        val errorMessage = entity["error"] as? String ?: "No error message"

        println("Error message from entity: '$errorMessage'")
        assertEquals("Checkout time cannot be before checkin time", errorMessage)
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
}
