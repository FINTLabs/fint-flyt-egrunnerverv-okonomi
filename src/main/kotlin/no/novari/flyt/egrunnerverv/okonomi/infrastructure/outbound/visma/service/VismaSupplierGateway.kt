package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.service

import no.novari.flyt.egrunnerverv.okonomi.domain.error.CreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.OrganizationToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierGatewayPort
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaClientException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaCreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaGetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaOrganizationToCompanyException
import org.springframework.stereotype.Component

@Component("visma")
class VismaSupplierGateway(
    private val client: VismaReskontroClient,
) : SupplierGatewayPort {
    override fun getOrCreate(
        supplier: Supplier,
        supplierIdentity: SupplierIdentity,
        tenantId: TenantId,
    ): SupplierSyncResult {
        try {
            val existingSupplier =
                client.getCustomerSupplierByIdentifier(
                    supplierIdentity,
                    tenantId,
                )

            if (existingSupplier == null) {
                client.createCustomerSupplier(supplier, supplierIdentity, tenantId)

                return SupplierSyncResult.Created
            } else {
                return SupplierSyncResult.Updated
            }
        } catch (e: VismaClientException) {
            when (e) {
                is VismaCreateSupplierException -> throw CreateSupplierException()
                is VismaGetSupplierException -> throw GetSupplierException()
                is VismaOrganizationToCompanyException -> throw OrganizationToCompanyException()
            }
        } catch (_: Exception) {
            throw GenericSupplierException("Klient feilet")
        }
    }
}
