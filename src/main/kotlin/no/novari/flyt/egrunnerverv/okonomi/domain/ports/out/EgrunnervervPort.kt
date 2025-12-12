package no.novari.flyt.egrunnerverv.okonomi.domain.ports.out

import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId

interface EgrunnervervPort {
    fun syncSupplier(
        supplierSyncResult: SupplierSyncResult,
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    )
}
