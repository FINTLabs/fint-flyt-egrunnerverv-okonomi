package no.novari.flyt.egrunnerverv.okonomi.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("novari.adapter.supplier")
data class TenantAdapterProperties(
    val byTenant: Map<String, String>,
)
