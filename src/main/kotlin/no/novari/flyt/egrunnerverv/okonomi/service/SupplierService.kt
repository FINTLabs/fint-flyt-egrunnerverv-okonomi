package no.novari.flyt.egrunnerverv.okonomi.service

import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.service.VismaReskontroClient
import no.novari.flyt.egrunnerverv.okonomi.model.enum.Organization
import no.novari.flyt.egrunnerverv.okonomi.model.payload.GetOrCreateSupplier
import org.springframework.stereotype.Service

@Service
class SupplierService(
    private val vismaReskontroClient: VismaReskontroClient,
) {
    fun getOrCreate(
        supplier: GetOrCreateSupplier,
        organization: Organization,
    ) {
        val identifier = getIdentifier(supplier)

        val existingSupplier =
            vismaReskontroClient.getCustomerSupplierByIdentifier(
                identifier,
                organization,
            )

        if (existingSupplier == null) {
            vismaReskontroClient.createCustomerSupplier(supplier, organization)
        }

        // TODO: Oppdater eGrunnerverv med ny info
    }

    private fun getIdentifier(supplier: GetOrCreateSupplier): String {
        return when {
            supplier.fodselsNummer.isNotBlank() -> supplier.fodselsNummer
            supplier.orgId.isNotBlank() -> supplier.orgId
            else -> throw IllegalArgumentException("Ukjent identifier: forventet fødselsnummer eller orgId")
        }
    }
}
