package no.novari.flyt.egrunnerverv.okonomi.application.supplier

import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncContext
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierSyncOutcome
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId

interface SyncSupplierUseCase {
    fun getOrCreate(
        supplier: Supplier,
        syncContext: SupplierSyncContext,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ): SupplierSyncOutcome
}
