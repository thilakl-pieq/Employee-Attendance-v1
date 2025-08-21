import com.codahale.metrics.health.HealthCheck
import org.jdbi.v3.core.Jdbi

class BasicHealthCheck(private val jdbi: Jdbi) : HealthCheck() {

    override fun check(): Result {
        return try {
            // Simple query to verify DB connectivity and responsiveness
            jdbi.withHandle<Int, Exception> { handle ->
                handle.createQuery("SELECT 1")
                    .mapTo(Int::class.java)
                    .one()
            }.let {
                if (it == 1) {
                    Result.healthy()
                } else {
                    Result.unhealthy("Unexpected result from DB health check query")
                }
            }
        } catch (e: Exception) {
            Result.unhealthy("Database connectivity failed: ${e.message}")
        }
    }
}
