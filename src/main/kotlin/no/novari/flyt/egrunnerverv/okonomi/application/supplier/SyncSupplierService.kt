package no.novari.flyt.egrunnerverv.okonomi.application.supplier

import no.novari.flyt.egrunnerverv.okonomi.application.tenant.TenantGatewayResolver
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncContext
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.EgrunnervervPort

class SyncSupplierService(
    private val tenantGatewayResolver: TenantGatewayResolver,
    private val egrunnervervPort: EgrunnervervPort,
) : SyncSupplierUseCase {
    override fun getOrCreate(
        supplier: Supplier,
        syncContext: SupplierSyncContext,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ): SupplierSyncOutcome {
        val supplierSyncOutcome =
            tenantGatewayResolver
                .resolve(tenantId)
                .getOrCreate(
                    supplier = supplier,
                    supplierIdentity = supplierIdentity,
                    tenantId = tenantId,
                )

        egrunnervervPort.syncSupplier(
            supplierSyncOutcome = supplierSyncOutcome,
            syncContext = syncContext,
            supplierIdentity = supplierIdentity,
            tenantId = tenantId,
        )

        return supplierSyncOutcome
    }
}
