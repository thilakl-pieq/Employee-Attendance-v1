package api

import java.time.LocalDateTime
import com.fasterxml.jackson.annotation.JsonFormat


data class CheckInRequest(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    val checkInDateTime: LocalDateTime
)

data class CheckOutRequest(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    val checkOutDateTime: LocalDateTime
)

