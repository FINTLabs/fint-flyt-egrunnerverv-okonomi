package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.novari.flyt.egrunnerverv.okonomi.domain.error.CreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GenericSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.GetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.OrganizationToCompanyException
import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.domain.ports.out.SupplierSyncResult
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaCreateSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaGetSupplierException
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error.VismaOrganizationToCompanyException
import org.springframework.web.client.RestClientException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VismaSupplierGatewayTest {
    private val client = mockk<VismaReskontroClient>()
    private val gateway = VismaSupplierGateway(client)
    private val supplier =
        Supplier(
            name = "Supplier",
            kontoNummer = "1234.56.78901",
            street = "Street 1",
            zip = "0010",
            city = "Oslo",
            email = "contact@test.no",
        )
    private val identity = SupplierIdentity.OrgId("999999999")
    private val tenantId = TenantId.NOVARI

    @Test
    fun `creates supplier when not found`() {
        every { client.getCustomerSupplierByIdentifier(identity, tenantId) } returns null
        every {
            client.createCustomerSupplier(
                supplier = supplier,
                supplierIdentity = identity,
                tenantId = tenantId,
            )
        } returns Unit

        val result = gateway.getOrCreate(supplier = supplier, supplierIdentity = identity, tenantId = tenantId)

        assertEquals(SupplierSyncResult.Created, result)
        verify(exactly = 1) {
            client.createCustomerSupplier(
                supplier = supplier,
                supplierIdentity = identity,
                tenantId = tenantId,
            )
        }
    }

    @Test
    fun `updates supplier when already exists`() {
        every { client.getCustomerSupplierByIdentifier(identity, tenantId) } returns supplier

        val result = gateway.getOrCreate(supplier = supplier, supplierIdentity = identity, tenantId = tenantId)

        assertEquals(SupplierSyncResult.Updated, result)
        verify(exactly = 0) { client.createCustomerSupplier(any(), any(), any()) }
    }

    @Test
    fun `translates Visma create exception`() {
        every { client.getCustomerSupplierByIdentifier(identity, tenantId) } returns null
        every {
            client.createCustomerSupplier(
                supplier = supplier,
                supplierIdentity = identity,
                tenantId = tenantId,
            )
        } throws VismaCreateSupplierException()

        assertFailsWith<CreateSupplierException> {
            gateway.getOrCreate(supplier = supplier, supplierIdentity = identity, tenantId = tenantId)
        }
    }

    @Test
    fun `translates Visma get exception`() {
        every { client.getCustomerSupplierByIdentifier(identity, tenantId) } throws VismaGetSupplierException()

        assertFailsWith<GetSupplierException> {
            gateway.getOrCreate(supplier, identity, tenantId)
        }
    }

    @Test
    fun `translates organization to company exception`() {
        every { client.getCustomerSupplierByIdentifier(identity, tenantId) } throws
            VismaOrganizationToCompanyException(
                tenantId,
            )

        assertFailsWith<OrganizationToCompanyException> {
            gateway.getOrCreate(supplier, identity, tenantId)
        }
    }

    @Test
    fun `wraps unexpected exception`() {
        every { client.getCustomerSupplierByIdentifier(identity, tenantId) } throws RestClientException("boom")

        assertFailsWith<GenericSupplierException> {
            gateway.getOrCreate(supplier = supplier, supplierIdentity = identity, tenantId = tenantId)
        }
    }
}
