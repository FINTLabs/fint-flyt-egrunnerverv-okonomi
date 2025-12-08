package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.mapper

import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.CustomerSupplier
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.CustomerSuppliers
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.PostalAddress
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.SupplierType
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.VUXml
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
        return Supplier(
            name = customerSupplier.csName ?: "",
            kontoNummer = customerSupplier.bankAccount ?: "",
            street = customerSupplier.csAddress?.joinToString(", ") ?: "",
            zip = customerSupplier.csPostalAddress?.zipCode ?: "",
            city = customerSupplier.csPostalAddress?.city ?: "",
            email = customerSupplier.csEmail ?: "",
        )
    }

    fun mapToVismaRequest(
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
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
                csId = null,
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
                orgNo = supplierIdentity.value,
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
}
