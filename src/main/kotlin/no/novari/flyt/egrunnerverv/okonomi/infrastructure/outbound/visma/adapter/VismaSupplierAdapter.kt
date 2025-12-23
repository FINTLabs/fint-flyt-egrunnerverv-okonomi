package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.adapter

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.novari.flyt.egrunnerverv.okonomi.domain.error.CreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.IdentifierTooLongException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.TenantToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.domain.logging.toMaskedLogMap
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierGatewayPort
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaClientException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaCreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaGetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaIdentifierTooLongException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaTenantToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.service.VismaReskontroClient
import org.springframework.stereotype.Component

@Component("visma")
class VismaSupplierAdapter(
    private val client: VismaReskontroClient,
    private val logger: KLogger = KotlinLogging.logger {},
) : SupplierGatewayPort {
    override fun getOrCreate(
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ): SupplierSyncOutcome {
        try {
            val existingSupplier = client.getCustomerSupplierByIdentifier(supplierIdentity, tenantId)

            if (existingSupplier == null) {
                client.createCustomerSupplier(
                    supplier = supplier,
                    supplierIdentity = supplierIdentity,
                    tenantId = tenantId,
                )
                val createdSupplier =
                    client.getCustomerSupplierByIdentifier(supplierIdentity, tenantId)
                        ?: throw VismaGetSupplierException(tenantId)

                return SupplierSyncOutcome(SupplierSyncResult.Created, createdSupplier)
            }

            return SupplierSyncOutcome(SupplierSyncResult.Updated, existingSupplier)
        } catch (vce: VismaClientException) {
            when (vce) {
                is VismaCreateSupplierException -> throw CreateSupplierException(vce)
                is VismaGetSupplierException -> throw GetSupplierException(vce)
                is VismaTenantToCompanyException -> throw TenantToCompanyException(vce)
                is VismaIdentifierTooLongException -> throw IdentifierTooLongException()
            }
        } catch (e: Exception) {
            logger.atError {
                message = "Ukjent feil i kommunikasjonen med Visma-klienten"
                arguments =
                    arrayOf(
                        kv("supplier", supplier.toMaskedLogMap()),
                        kv("supplierIdentity", supplierIdentity.toMaskedLogMap()),
                        kv("tenant", tenantId),
                        kv("cause", e),
                    )
            }
            throw GenericSupplierException("Ukjent feil i kommunikasjonen med Visma-klienten", e)
        }
    }
}
