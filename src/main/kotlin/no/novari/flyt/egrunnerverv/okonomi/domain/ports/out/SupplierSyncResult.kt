package no.novari.flyt.egrunnerverv.okonomi.domain.ports.out

sealed interface SupplierSyncResult {
    object Created : SupplierSyncResult

    object Updated : SupplierSyncResult
}
