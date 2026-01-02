package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.adapter

import io.github.oshai.kotlinlogging.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.ServiceNowSyncException
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncContext
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.EgrunnervervPort
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.error.MissingExternalIdException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.error.ServiceNowClientException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.error.ServiceNowPatchConsumerException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.service.ServiceNowClient
import org.springframework.stereotype.Component

@Component
class ServiceNowAdapter(
    private val client: ServiceNowClient,
) : EgrunnervervPort {
    private val logger = KotlinLogging.logger {}

    override fun syncSupplier(
        supplierSyncOutcome: SupplierSyncOutcome,
        syncContext: SupplierSyncContext,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ) {
        try {
            client.syncSupplier(
                supplierSyncOutcome = supplierSyncOutcome,
                syncContext = syncContext,
                tenantId = tenantId,
            )
        } catch (sne: ServiceNowClientException) {
            when (sne) {
                is ServiceNowPatchConsumerException -> {
                    logger.atError {
                        message = "Klarte ikke å oppdatere ServiceNow med ekstern ID"
                        arguments =
                            arrayOf(
                                kv("sys_id", syncContext.serviceNowSysId),
                                kv("external_id", supplierSyncOutcome.supplier.externalId),
                                kv("cause", sne),
                            )
                    }
                    throw ServiceNowSyncException(
                        "Klarte ikke å oppdatere ServiceNow med ekstern ID",
                        sne,
                    )
                }

                is MissingExternalIdException -> {
                    logger.atError {
                        message = "Klarte ikke å oppdatere ServiceNow fordi ekstern ID mangler"
                        arguments =
                            arrayOf(
                                kv("sys_id", syncContext.serviceNowSysId),
                                kv("cause", sne),
                            )
                    }
                    throw ServiceNowSyncException(
                        "Klarte ikke å oppdatere ServiceNow fordi ekstern ID mangler",
                        sne,
                    )
                }
            }
        } catch (e: Exception) {
            logger.atError {
                message = "Ukjent feil i kommunikasjonen med ServiceNow-klienten"
                arguments =
                    arrayOf(
                        kv("sys_id", syncContext.serviceNowSysId),
                        kv("external_id", supplierSyncOutcome.supplier.externalId),
                        kv("cause", e),
                    )
            }
            throw GenericSupplierException("Ukjent feil i kommunikasjonen med ServiceNow-klienten", e)
        }
    }
}
