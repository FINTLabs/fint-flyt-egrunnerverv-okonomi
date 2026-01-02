package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncContext
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.error.MissingExternalIdException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.error.ServiceNowPatchConsumerException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.model.PatchConsumer
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.model.PatchConsumerResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.retry.support.RetryTemplate
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body

@Service
class ServiceNowClient(
    @param:Qualifier("serviceNowRestClient") private val restClient: RestClient,
    @param:Qualifier("serviceNowRetryTemplate") private val retryTemplate: RetryTemplate,
    private val meterRegistry: MeterRegistry,
) {
    fun syncSupplier(
        supplierSyncOutcome: SupplierSyncOutcome,
        syncContext: SupplierSyncContext,
        tenantId: TenantId,
    ) {
        val sample = Timer.start(meterRegistry)
        var result = "unknown"
        try {
            retryTemplate.execute<Unit, Exception> {
                val externalId =
                    supplierSyncOutcome.supplier.externalId
                        ?: throw MissingExternalIdException(syncContext.serviceNowSysId)

                try {
                    val payload = PatchConsumer(externalId.value)
                    restClient
                        .patch()
                        .uri(
                            "/csm_consumer/{sys_id}?sysparam_fields=sys_id,u_leverandor_id",
                            syncContext.serviceNowSysId.value,
                        ).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .body(payload)
                        .retrieve()
                        .body<PatchConsumerResponse>()
                        ?: throw ServiceNowPatchConsumerException(syncContext.serviceNowSysId)
                } catch (e: Exception) {
                    if (e is ServiceNowPatchConsumerException) {
                        throw e
                    }
                    throw ServiceNowPatchConsumerException(syncContext.serviceNowSysId, e)
                }
            }

            result = "success"
        } catch (e: MissingExternalIdException) {
            result = "missing_external_id"
            throw e
        } catch (e: Exception) {
            result = "error"
            throw e
        } finally {
            meterRegistry
                .counter(
                    "servicenow.patch.count",
                    "tenant",
                    tenantId.id,
                    "result",
                    result,
                ).increment()

            sample.stop(
                Timer
                    .builder("servicenow.patch.latency")
                    .publishPercentileHistogram()
                    .tag("tenant", tenantId.id)
                    .tag("result", result)
                    .register(meterRegistry),
            )
        }
    }
}
