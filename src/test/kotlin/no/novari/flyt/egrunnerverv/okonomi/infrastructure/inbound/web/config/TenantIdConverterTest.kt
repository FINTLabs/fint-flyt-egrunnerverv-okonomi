package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.config

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error.InvalidTenantException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TenantIdConverterTest {
    private val converter = TenantIdConverter()

    @Test
    fun `convert resolves tenant ignoring case`() {
        val tenant = converter.convert("NoVaRi-No")

        assertEquals(TenantId.NOVARI, tenant)
    }

    @Test
    fun `convert throws on unknown tenant`() {
        assertFailsWith<InvalidTenantException> {
            converter.convert("unknown-tenant")
        }
    }
}
