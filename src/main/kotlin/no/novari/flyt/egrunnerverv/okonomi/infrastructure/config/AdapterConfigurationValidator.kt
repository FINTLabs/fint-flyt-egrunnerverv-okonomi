package no.novari.flyt.egrunnerverv.okonomi.infrastructure.config

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config.VismaProperties
import org.springframework.context.annotation.Configuration

@Configuration
class AdapterConfigurationValidator(
    private val adapterProps: AdapterSupplierProperties,
    private val vismaProps: VismaProperties,
) {
    private val logger = KotlinLogging.logger {}

    @PostConstruct
    fun validateTenants() {
        validateVismaTenants()
    }

    private fun validateVismaTenants() {
        val vismaTenants = adapterProps.byTenant.filterValues { it == VISMA_ADAPTER_NAME }.keys
        val companyTenants = vismaProps.company.byTenant.keys

        val missingCompany = vismaTenants - companyTenants
        val unusedCompany = companyTenants - vismaTenants

        require(missingCompany.isEmpty()) {
            "Mangler selskapsmapping mot Visma for tenant(s): $missingCompany."
        }
        require(unusedCompany.isEmpty()) {
            "Selskapsmapping mot Visma er definert for tenanter som ikke er koblet til Visma-adapteret: $unusedCompany."
        }

        if (vismaTenants.isEmpty()) {
            logger.warn { "Ingen tenanter er konfigurert til å bruke Visma-adapteret" }
        }
    }

    companion object {
        private const val VISMA_ADAPTER_NAME = "visma"
    }
}
