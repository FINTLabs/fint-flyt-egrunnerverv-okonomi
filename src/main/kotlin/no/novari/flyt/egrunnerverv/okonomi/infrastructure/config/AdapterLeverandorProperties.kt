package no.novari.flyt.egrunnerverv.okonomi.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("adapter.leverandor")
data class AdapterLeverandorProperties(
    val byTenant: Map<String, String>,
)
