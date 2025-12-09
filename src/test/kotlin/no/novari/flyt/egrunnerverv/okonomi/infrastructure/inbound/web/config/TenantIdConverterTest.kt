package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.config

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.TenantAdapterProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error.InvalidTenantException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TenantIdConverterTest {
    private val tenantAdapterProperties: TenantAdapterProperties =
        TenantAdapterProperties(
            byTenant = mapOf("novari-no" to "novari"),
        )
    private val converter = TenantIdConverter(tenantAdapterProperties)

    @Test
    fun `convert returns tenant when key exists`() {
        val tenant = converter.convert("novari-no")
        assertEquals(TenantId("novari-no"), tenant)
    }

    @Test
    fun `convert trims whitespace`() {
        val tenant = converter.convert("  novari-no  ")
        assertEquals(TenantId("novari-no"), tenant)
    }

    @Test
    fun `convert throws on case mismatch`() {
        assertFailsWith<InvalidTenantException> {
            converter.convert("NoVaRi-No")
        }
    }

    @Test
    fun `convert throws on unknown tenant`() {
        assertFailsWith<InvalidTenantException> {
            converter.convert("unknown-tenant")
        }
    }

    @Test
    fun `convert throws on blank`() {
        assertFailsWith<InvalidTenantException> {
            converter.convert("   ")
        }
    }
}
