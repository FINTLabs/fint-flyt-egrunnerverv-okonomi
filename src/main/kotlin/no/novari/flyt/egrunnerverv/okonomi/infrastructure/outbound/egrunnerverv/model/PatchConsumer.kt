package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PatchConsumer(
    @get:JsonProperty("u_leverandor_id")
    val supplierId: String,
)
