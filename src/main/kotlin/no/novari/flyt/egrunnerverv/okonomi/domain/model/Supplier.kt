package no.novari.flyt.egrunnerverv.okonomi.domain.model

import no.novari.flyt.egrunnerverv.okonomi.domain.logging.LogMasked
import no.novari.flyt.egrunnerverv.okonomi.domain.logging.MaskType

data class Supplier(
    val externalId: ExternalSupplierId?,
    @LogMasked(type = MaskType.KEEP_FIRST, first = 4)
    val name: String,
    @LogMasked(type = MaskType.KEEP_LAST, last = 4)
    val kontoNummer: String,
    @LogMasked(type = MaskType.KEEP_FIRST_AND_LAST, first = 2, last = 4)
    val street: String,
    @LogMasked(type = MaskType.FULL)
    val zip: String,
    @LogMasked(type = MaskType.FULL)
    val city: String,
    @LogMasked(type = MaskType.KEEP_FIRST_AND_LAST, first = 4, last = 8)
    val email: String,
)
