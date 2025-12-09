package no.novari.flyt.egrunnerverv.okonomi.domain.model

import no.novari.flyt.egrunnerverv.okonomi.domain.error.MissingIdentifierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MultipleIdentifiersException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class SupplierIdentityTest {
    @Test
    fun `from returns Fodselsnummer when only fnr present`() {
        val identity = SupplierIdentity.from(fnr = " 12345678901 ", org = null)

        assertIs<SupplierIdentity.Fodselsnummer>(identity)
        assertEquals("12345678901", identity.value)
    }

    @Test
    fun `from returns OrgId when only org present`() {
        val identity = SupplierIdentity.from(fnr = null, org = " 999999999 ")

        assertIs<SupplierIdentity.OrgId>(identity)
        assertEquals("999999999", identity.value)
    }

    @Test
    fun `from throws when both identifiers missing`() {
        assertFailsWith<MissingIdentifierException> {
            SupplierIdentity.from(fnr = " ", org = null)
        }
    }

    @Test
    fun `from throws when both identifiers provided`() {
        assertFailsWith<MultipleIdentifiersException> {
            SupplierIdentity.from(fnr = "123", org = "321")
        }
    }

    @Test
    fun `fromValue detects fnr vs org and trims`() {
        val fnrIdentity = SupplierIdentity.fromValue(" 12345678901 ")
        val orgIdentity = SupplierIdentity.fromValue(" 123456789 ")

        assertIs<SupplierIdentity.Fodselsnummer>(fnrIdentity)
        assertEquals("12345678901", fnrIdentity.value)
        assertIs<SupplierIdentity.OrgId>(orgIdentity)
        assertEquals("123456789", orgIdentity.value)
    }
}
