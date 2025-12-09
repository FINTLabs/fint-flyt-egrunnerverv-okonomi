package no.novari.flyt.egrunnerverv.okonomi.application.supplier

import no.novari.flyt.egrunnerverv.okonomi.application.tenant.TenantGatewayResolver
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import org.springframework.stereotype.Service

class SyncSupplierService(
    private val tenantGatewayResolver: TenantGatewayResolver,
) : SyncSupplierUseCase {
    override fun getOrCreate(
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ): SupplierSyncResult {
        val supplierSyncResult =
            tenantGatewayResolver.resolve(tenantId).getOrCreate(
                supplier = supplier,
                supplierIdentity = supplierIdentity,
                tenantId = tenantId,
            )

        // TODO: Oppdater eGrunnerverv med ny info

        return supplierSyncResult
    }
}
