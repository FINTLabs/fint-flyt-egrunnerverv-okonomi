package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.mapper

import no.novari.flyt.egrunnerverv.okonomi.domain.model.Supplier
import no.novari.flyt.egrunnerverv.okonomi.domain.model.SupplierIdentity
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.CustomerSupplier
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.CustomerSuppliers
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.PostalAddress
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.SupplierType
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.model.VUXml
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SupplierMapperTest {
    private val mapper = SupplierMapper()

    @Test
    fun `mapSingleSupplier returns null when response is empty`() {
        val result = mapper.mapSingleSupplier(VUXml(customerSuppliers = emptyList()))

        assertNull(result)
    }

    @Test
    fun `mapSingleSupplier errors on multiple supplier groups`() {
        val xml =
            VUXml(
                customerSuppliers =
                    listOf(
                        CustomerSuppliers(company = "123", division = "0"),
                        CustomerSuppliers(company = "456", division = "0"),
                    ),
            )

        assertFailsWith<IllegalStateException> { mapper.mapSingleSupplier(xml) }
    }

    @Test
    fun `mapSingleSupplier errors on multiple customer entries`() {
        val xml =
            VUXml(
                customerSuppliers =
                    listOf(
                        CustomerSuppliers(
                            company = "123",
                            division = "0",
                            customerSupplier =
                                listOf(
                                    CustomerSupplier(csType = "L"),
                                    CustomerSupplier(csType = "L"),
                                ),
                        ),
                    ),
            )

        assertFailsWith<IllegalStateException> { mapper.mapSingleSupplier(xml) }
    }

    @Test
    fun `mapSingleSupplier returns supplier when single entry`() {
        val xml =
            VUXml(
                customerSuppliers =
                    listOf(
                        CustomerSuppliers(
                            company = "123",
                            division = "0",
                            customerSupplier =
                                listOf(
                                    CustomerSupplier(
                                        csType = "L",
                                        csName = "Test Leverandør",
                                        csAddress = listOf("Inngang 1", "Gate 2"),
                                        csPostalAddress = PostalAddress(zipCode = "0010", city = "Oslo"),
                                        csEmail = "post@test.no",
                                        bankAccount = "1234.56.78901",
                                        orgNo = "999999999",
                                    ),
                                ),
                        ),
                    ),
            )

        val supplier = mapper.mapSingleSupplier(xml)

        assertNotNull(supplier)
        assertEquals("Test Leverandør", supplier.name)
        assertEquals("1234.56.78901", supplier.kontoNummer)
        assertEquals("Inngang 1, Gate 2", supplier.street)
        assertEquals("0010", supplier.zip)
        assertEquals("Oslo", supplier.city)
        assertEquals("post@test.no", supplier.email)
    }

    @Test
    fun `mapSingleSupplier returns null when group exists but no customer`() {
        val xml =
            VUXml(
                customerSuppliers =
                    listOf(
                        CustomerSuppliers(
                            company = "123",
                            division = "0",
                            customerSupplier = emptyList(),
                        ),
                    ),
            )

        val supplier = mapper.mapSingleSupplier(xml)

        assertNull(supplier)
    }

    @Test
    fun `mapToVismaRequest maps fields and splits street`() {
        val supplier =
            Supplier(
                name = "Leverandør AS",
                kontoNummer = "1234.56.78901",
                street = "Oppgang 1, Gate 2",
                zip = "0010",
                city = "Oslo",
                email = "post@test.no",
            )
        val identity = SupplierIdentity.OrgId("999999999")

        val request =
            mapper.mapToVismaRequest(
                supplier = supplier,
                supplierIdentity = identity,
                company = "123",
                division = "0",
                type = SupplierType.LEVERANDOR,
            )

        val customerSuppliers = request.customerSuppliers.single()
        val customer = customerSuppliers.customerSupplier.single()
        assertEquals("123", customerSuppliers.company)
        assertEquals("0", customerSuppliers.division)
        assertEquals(listOf("Oppgang 1", "Gate 2"), customer.csAddress)
        assertEquals("999999999", customer.orgNo)
        assertEquals("L", customer.csType)
    }
}
