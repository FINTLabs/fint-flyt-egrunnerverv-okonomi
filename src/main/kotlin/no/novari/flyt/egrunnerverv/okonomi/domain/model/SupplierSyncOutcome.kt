package no.novari.flyt.egrunnerverv.okonomi.domain.model

import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult

data class SupplierSyncOutcome(
    val result: SupplierSyncResult,
    val supplier: Supplier,
)
