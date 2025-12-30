package no.novari.flyt.egrunnerverv.okonomi.infrastructure.config

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.config.ServiceNowProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config.VismaProperties
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

@Configuration
class AdapterConfigurationValidator(
    private val adapterProps: AdapterSupplierProperties,
    private val vismaProps: VismaProperties,
    private val serviceNowProps: ServiceNowProperties,
    private val clientRegistrationRepository: ClientRegistrationRepository,
) {
    private val logger = KotlinLogging.logger {}

    @PostConstruct
    fun validateTenants() {
        validateOrgNoMappings()
        validateAdapterMappings()
        validateTenants(VISMA_ADAPTER_NAME)
        validateVismaTenantFields()
        validateVismaRegistrations()
        validateServiceNowFields()
        validateServiceNowRegistration()
    }

    private fun validateOrgNoMappings() {
        val tenants = adapterProps.byTenant.keys
        val missingTenants = adapterProps.byOrgNo.values.toSet() - tenants

        if (missingTenants.isNotEmpty()) {
            failValidation("Org-nr peker på ukjente tenanter: $missingTenants.")
        }
    }

    private fun validateAdapterMappings() {
        val configuredAdapters = setOf(VISMA_ADAPTER_NAME, SERVICENOW_ADAPTER_NAME)
        val unknownAdapters = adapterProps.byTenant.values.toSet() - configuredAdapters

        if (unknownAdapters.isNotEmpty()) {
            failValidation("Ukjente adaptere i by-tenant: $unknownAdapters.")
        }
    }

    private fun validateTenants(adapterName: String) {
        val tenants = adapterProps.byTenant.filterValues { it == adapterName }.keys
        val configuredTenants = vismaProps.tenants.keys

        val missingConfig = tenants - configuredTenants
        val unusedConfig = configuredTenants - tenants

        if (missingConfig.isNotEmpty()) {
            failValidation("Mangler konfigurasjon for tenant(s): $missingConfig.")
        }
        if (unusedConfig.isNotEmpty()) {
            failValidation(
                "Konfigurasjon er definert for tenanter som ikke er koblet til adapteret: $unusedConfig.",
            )
        }

        if (tenants.isEmpty()) {
            logger.warn { "Ingen tenanter er konfigurert til å bruke adapteret $adapterName" }
        }
    }

    private fun validateVismaRegistrations() {
        val missingRegistrations =
            vismaProps.tenants
                .values
                .map { it.registrationId }
                .filter { clientRegistrationRepository.findByRegistrationId(it) == null }
                .toSet()

        if (missingRegistrations.isNotEmpty()) {
            failValidation(
                "Mangler OAuth2-registrering(er) for Visma: $missingRegistrations. " +
                    "Forventet spring.security.oauth2.client.registration.<id> for hver.",
            )
        }
    }

    private fun validateVismaTenantFields() {
        vismaProps.tenants.forEach { (tenantId, tenant) ->
            if (tenant.baseUrl.isBlank()) {
                failValidation("Visma base-url er tom for tenant $tenantId.")
            }
            if (tenant.legacyAuth.isBlank()) {
                failValidation("Visma legacy-auth er tom for tenant $tenantId.")
            }
            val companyNumber =
                tenant.company.toIntOrNull()
                    ?: failValidation("Visma company er ikke et gyldig tall for tenant $tenantId.")
            if (companyNumber <= 1) {
                failValidation("Visma company må være et tall større enn 1 for tenant $tenantId.")
            }
        }
    }

    private fun validateServiceNowFields() {
        if (serviceNowProps.baseUrl.isBlank()) {
            failValidation("ServiceNow base-url er tom.")
        }
        if (serviceNowProps.oauth2.username.isBlank()) {
            failValidation("ServiceNow oauth2.username er tom.")
        }
        if (serviceNowProps.oauth2.password.isBlank()) {
            failValidation("ServiceNow oauth2.password er tom.")
        }
    }

    private fun validateServiceNowRegistration() {
        val registrationId = serviceNowProps.registrationId
        if (clientRegistrationRepository.findByRegistrationId(registrationId) == null) {
            failValidation(
                "Mangler OAuth2-registrering for ServiceNow: $registrationId. " +
                    "Forventet spring.security.oauth2.client.registration.$registrationId.",
            )
        }
    }

    private fun failValidation(message: String): Nothing {
        logger.error { "Konfigurasjonsfeil: $message" }
        throw IllegalStateException("Konfigurasjonsfeil: $message")
    }

    companion object {
        private const val VISMA_ADAPTER_NAME = "visma"
        private const val SERVICENOW_ADAPTER_NAME = "servicenow"
    }
}
