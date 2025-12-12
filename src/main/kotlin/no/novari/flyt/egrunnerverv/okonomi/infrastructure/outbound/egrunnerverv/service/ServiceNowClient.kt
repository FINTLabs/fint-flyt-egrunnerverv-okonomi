package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.service

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.config.ServiceNowProperties
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class ServiceNowClient(
    @param:Qualifier("serviceNowRestClient") private val restClient: RestClient,
    private val props: ServiceNowProperties,
    private val logger: KLogger = KotlinLogging.logger {},
) {
    @Retryable(
        maxAttemptsExpression = "\${adapter.adapters.servicenow.retry.max-attempts}",
        backoff =
            Backoff(
                delayExpression = "\${adapter.adapters.servicenow.retry.initial-interval-ms}",
                maxDelayExpression = "10000",
            ),
    )
    fun syncSupplier(
        supplierSyncResult: SupplierSyncResult,
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ) {
    }
}
