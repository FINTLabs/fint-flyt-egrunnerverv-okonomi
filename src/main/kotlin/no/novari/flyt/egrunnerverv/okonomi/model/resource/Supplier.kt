package no.novari.flyt.egrunnerverv.okonomi.model.resource

import com.fasterxml.jackson.annotation.JsonProperty
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.logging.LogMasked
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.logging.MaskType

data class Supplier(
    @get:JsonProperty("sys_id")
    val sysId: String,
    @get:JsonProperty("u_fdselsnummer")
    @LogMasked(type = MaskType.FULL)
    val fodselsNummer: String? = null,
    @get:JsonProperty("u_orgid")
    val orgId: String? = null,
    @LogMasked(type = MaskType.KEEP_FIRST, first = 4)
    val name: String,
    @get:JsonProperty("u_kontonummer")
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
