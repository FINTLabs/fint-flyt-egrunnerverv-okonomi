package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.adapter

import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.EgrunnervervPort
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.service.ServiceNowClient
import org.springframework.stereotype.Component

@Component
class ServiceNowAdapter(
    private val client: ServiceNowClient,
) : EgrunnervervPort {
    override fun syncSupplier(
        supplierSyncResult: SupplierSyncResult,
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ) {
        client.syncSupplier(supplierSyncResult, supplier, supplierIdentity, tenantId)
    }
}
