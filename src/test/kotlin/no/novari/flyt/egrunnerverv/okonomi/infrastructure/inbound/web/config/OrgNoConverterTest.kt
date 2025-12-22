package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.config

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.AdapterSupplierProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error.InvalidOrganizationNumberException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrgNoConverterTest {
    private val adapterSupplierProperties: AdapterSupplierProperties =
        AdapterSupplierProperties(
            byOrgNo = mapOf(999999999L to "novari-no"),
            byTenant = emptyMap(),
        )
    private val converter = OrgNoConverter(adapterSupplierProperties)

    @Test
    fun `convert returns orgNo when key exists`() {
        val tenant = converter.convert(999999999L)
        assertEquals(TenantId("novari-no"), tenant)
    }

    @Test
    fun `convert throws on unknown orgNo`() {
        assertFailsWith<InvalidOrganizationNumberException> {
            converter.convert(123456789L)
        }
    }

}
