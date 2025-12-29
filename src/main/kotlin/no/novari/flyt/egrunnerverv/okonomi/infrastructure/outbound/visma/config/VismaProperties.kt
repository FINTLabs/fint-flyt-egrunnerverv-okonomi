package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("adapter.adapters.visma")
data class VismaProperties(
    val tenants: Map<String, Tenant> = emptyMap(),
    val timeouts: Timeouts = Timeouts(),
    val retry: Retry = Retry(),
) {
    data class Tenant(
        val registrationId: String,
        val baseUrl: String,
        val legacyAuth: String,
        val company: String,
        val timeouts: Timeouts? = null,
        val retry: Retry? = null,
    )

    data class Timeouts(
        val connect: Duration = Duration.ofSeconds(5),
        val read: Duration = Duration.ofSeconds(60),
    )

    data class Retry(
        val maxAttempts: Int = 10,
        val initialIntervalMs: Long = 500,
        val multiplier: Double = 2.0,
        val maxIntervalMs: Long = 10000,
    )
}
