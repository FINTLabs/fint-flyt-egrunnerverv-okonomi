package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("adapter.adapters.servicenow")
data class ServiceNowProperties(
    val registrationId: String,
    val baseUrl: String,
    val oauth2: OAuth2Props,
    val timeouts: Timeouts = Timeouts(),
    val retry: Retry = Retry(),
) {
    data class OAuth2Props(
        val username: String,
        val password: String,
    )

    data class Timeouts(
        val connect: Duration = Duration.ofSeconds(5),
        val read: Duration = Duration.ofSeconds(30),
    )

    data class Retry(
        val maxAttempts: Int = 10,
        val initialIntervalMs: Long = 500,
        val multiplier: Double = 2.0,
        val maxIntervalMs: Long = 10000,
    )
}
