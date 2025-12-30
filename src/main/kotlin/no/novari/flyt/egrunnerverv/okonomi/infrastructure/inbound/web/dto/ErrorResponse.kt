package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ErrorResponse(
    @get:JsonProperty("error_code")
    val errorCode: Int,
    @get:JsonProperty("error_message")
    val errorMessage: String,
)
