package no.novari.flyt.egrunnerverv.okonomi.infrastructure.config

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config.VismaProperties
import org.springframework.context.annotation.Configuration

@Configuration
class AdapterConfigurationValidator(
    private val adapterProps: AdapterLeverandorProperties,
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
            "Mangler Visma company-mapping for tenants: $missingCompany"
        }
        require(unusedCompany.isEmpty()) {
            "Visma company-mapping definert for tenants som ikke er mappet til visma-adapter: $unusedCompany"
        }

        if (vismaTenants.isEmpty()) {
            logger.warn { "Ingen tenants er satt opp til å bruke visma-adapteret" }
        }
    }

    companion object {
        private const val VISMA_ADAPTER_NAME = "visma"
    }
}
