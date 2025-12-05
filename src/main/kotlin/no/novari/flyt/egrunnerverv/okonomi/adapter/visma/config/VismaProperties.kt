package no.novari.flyt.egrunnerverv.okonomi.adapter.visma.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "visma")
data class VismaProperties(
    val baseUrl: String,
    val legacyAuth: String,
    val oauth: OAuthProps,
    val companyByOrganization: Map<String, String>,
) {
    data class OAuthProps(
        val tokenUrl: String,
        val clientId: String,
        val clientSecret: String,
    )
}
