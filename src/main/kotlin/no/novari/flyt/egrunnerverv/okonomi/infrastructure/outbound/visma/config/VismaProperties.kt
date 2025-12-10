package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("adapter.adapters.visma")
data class VismaProperties(
    val baseUrl: String,
    val legacyAuth: String,
    val oauth: OAuthProps,
    val company: Company,
    val timeouts: Timeouts = Timeouts(),
    val retry: Retry = Retry(),
) {
    data class OAuthProps(
        val tokenUrl: String,
        val clientId: String,
        val clientSecret: String,
    )

    data class Company(
        val byTenant: Map<String, String>,
    )

    data class Timeouts(
        val connect: Duration = Duration.ofSeconds(5),
        val read: Duration = Duration.ofSeconds(30),
    )

    data class Retry(
        val maxAttempts: Int = 5,
        val initialIntervalMs: Long = 500,
    )
}
