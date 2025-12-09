package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("visma")
data class VismaProperties(
    val baseUrl: String,
    val legacyAuth: String,
    val oauth: OAuthProps,
    val company: Company,
) {
    data class OAuthProps(
        val tokenUrl: String,
        val clientId: String,
        val clientSecret: String,
    )

    data class Company(
        val byTenant: Map<String, String>,
    )
}
