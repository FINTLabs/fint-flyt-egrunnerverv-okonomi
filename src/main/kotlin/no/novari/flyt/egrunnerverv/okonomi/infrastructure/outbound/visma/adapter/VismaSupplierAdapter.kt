package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.adapter

import no.novari.flyt.egrunnerverv.okonomi.domain.error.CreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.TenantToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierGatewayPort
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaClientException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaCreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaGetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaTenantToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.service.VismaReskontroClient
import org.springframework.stereotype.Component

@Component("visma")
class VismaSupplierAdapter(
    private val client: VismaReskontroClient,
) : SupplierGatewayPort {
    override fun getOrCreate(
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ): SupplierSyncResult {
        try {
            val existingSupplier = client.getCustomerSupplierByIdentifier(supplierIdentity, tenantId)

            if (existingSupplier == null) {
                client.createCustomerSupplier(supplier, supplierIdentity, tenantId)

                return SupplierSyncResult.Created
            } else {
                return SupplierSyncResult.Updated
            }
        } catch (vce: VismaClientException) {
            when (vce) {
                is VismaCreateSupplierException -> throw CreateSupplierException(vce)
                is VismaGetSupplierException -> throw GetSupplierException(vce)
                is VismaTenantToCompanyException -> throw TenantToCompanyException(vce)
            }
        } catch (e: Exception) {
            throw GenericSupplierException("Ukjent feil i kommunikasjonen med Visma-klienten", e)
        }
    }
}
