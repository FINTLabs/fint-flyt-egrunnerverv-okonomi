package no.novari.flyt.egrunnerverv.okonomi.domain.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LogMaskExtensionsTest {
    data class MaskedSample(
        @LogMasked val full: String,
        @LogMasked(type = MaskType.KEEP_LAST, last = 4) val keepLast: String,
        @LogMasked(type = MaskType.KEEP_FIRST, first = 3) val keepFirst: String,
        @LogMasked(type = MaskType.KEEP_FIRST_AND_LAST, first = 2, last = 2) val keepFirstAndLast: String,
        val plain: String,
        @LogMasked(type = MaskType.KEEP_FIRST_AND_LAST, first = 4, last = 4) val shortValue: String,
        @LogMasked val nullable: String?,
        @LogMasked val nonString: Int,
    )

    @Test
    fun `toMaskedLogMap masks and preserves fields`() {
        val sample =
            MaskedSample(
                full = "sensitive",
                keepLast = "12345678",
                keepFirst = "12345678",
                keepFirstAndLast = "12345678",
                plain = "visible",
                shortValue = "123",
                nullable = null,
                nonString = 5,
            )

        val masked = sample.toMaskedLogMap()

        assertEquals("********", masked["full"])
        assertEquals("****5678", masked["keepLast"])
        assertEquals("123*****", masked["keepFirst"])
        assertEquals("12****78", masked["keepFirstAndLast"])
        assertEquals("visible", masked["plain"])
        assertEquals("123", masked["shortValue"])
        assertNull(masked["nullable"])
        assertEquals("***", masked["nonString"])
    }
}
