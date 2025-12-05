package no.novari.flyt.egrunnerverv.okonomi.adapter.visma.mapper

import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.model.CustomerSupplier
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.model.CustomerSuppliers
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.model.PostalAddress
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.model.VUXml
import no.novari.flyt.egrunnerverv.okonomi.model.enum.SupplierType
import no.novari.flyt.egrunnerverv.okonomi.model.payload.GetOrCreateSupplier
import no.novari.flyt.egrunnerverv.okonomi.model.resource.Supplier
import org.springframework.stereotype.Component

@Component
class SupplierMapper {
    fun mapSingleSupplier(vuXml: VUXml): Supplier? {
        val suppliersList = vuXml.customerSuppliers

        val customerSuppliers =
            suppliersList.singleOrNull()
                ?: if (suppliersList.isEmpty()) {
                    return null
                } else {
                    error("Forventet nøyaktig 1 <customerSuppliers>, men fikk ${suppliersList.size}")
                }

        val customerList = customerSuppliers.customerSupplier

        val customerSupplier =
            customerList.singleOrNull()
                ?: if (customerList.isEmpty()) {
                    return null
                } else {
                    error("Forventet nøyaktig 1 <customerSupplier>, men fikk ${customerList.size}")
                }

        return mapToSupplier(customerSupplier)
    }

    fun mapToSupplier(customerSupplier: CustomerSupplier): Supplier {
        val (fodselsNummer, orgId) = splitOrgNo(customerSupplier.orgNo)
        return Supplier(
            sysId = customerSupplier.csId,
            fodselsNummer = fodselsNummer,
            orgId = orgId,
            name = customerSupplier.csName ?: "",
            kontoNummer = customerSupplier.bankAccount ?: "",
            street = customerSupplier.csAddress?.joinToString(", ") ?: "",
            zip = customerSupplier.csPostalAddress?.zipCode ?: "",
            city = customerSupplier.csPostalAddress?.city ?: "",
            // country = "", // Not stored/used/returned by Visma
            // mobilePhone = "", // // Not stored/used/returned by Visma
            email = customerSupplier.csEmail ?: "",
            // kommentar = "", // // Not stored/used/returned by Visma
        )
    }

    fun mapToVismaRequest(
        supplier: GetOrCreateSupplier,
        company: String,
        division: String,
        type: SupplierType,
    ): VUXml {
        val postalAddress =
            PostalAddress(
                zipCode = supplier.zip,
                city = supplier.city,
            )

        val customerSupplier =
            CustomerSupplier(
                csType = type.id,
                csId = supplier.sysId,
                csName = supplier.name,
                csAddress =
                    supplier.street
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() },
                csPostalAddress = postalAddress,
                csEmail = supplier.email,
                csWWW = null,
                bankAccount = supplier.kontoNummer,
                orgNo = buildOrgNo(supplier),
            )

        val customerSuppliers =
            CustomerSuppliers(
                company = company,
                division = division,
                customerSupplier = listOf(customerSupplier),
            )

        return VUXml(
            customerSuppliers = listOf(customerSuppliers),
        )
    }

    private fun splitOrgNo(orgNo: String?): Pair<String, String> {
        return if (orgNo == null) {
            "" to ""
        } else if (orgNo.length == FODSELSNUMMER_LENGDE) {
            orgNo to ""
        } else {
            "" to orgNo
        }
    }

    private fun buildOrgNo(supplier: GetOrCreateSupplier): String {
        return when {
            supplier.fodselsNummer.isNotBlank() -> supplier.fodselsNummer
            supplier.orgId.isNotBlank() -> supplier.orgId
            else -> ""
        }
    }

    companion object {
        private const val FODSELSNUMMER_LENGDE = 11
    }
}
