package no.novari.flyt.egrunnerverv.okonomi.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("adapter.supplier")
data class AdapterSupplierProperties(
    val byTenant: Map<String, String>,
    val byOrgNo: Map<Long, String>,
)
