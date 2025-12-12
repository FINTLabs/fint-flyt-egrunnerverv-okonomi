package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SupplierRequest(
    @field:Pattern(regexp = "^$|^[0-9]{11}$", message = "fodselsnummer må stå tomt eller inneholde nøyaktig 11 sifre")
    @get:JsonProperty("u_fdselsnummer")
    val fodselsNummer: String? = null,
    @field:Pattern(regexp = "^$|^[0-9]{9}$", message = "orgId må stå tomt eller inneholde nøyaktig 9 sifre")
    @get:JsonProperty("u_orgid")
    val orgId: String? = null,
    @field:NotBlank
    val name: String,
    @get:JsonProperty("u_kontonummer")
    @field:NotBlank
    val kontoNummer: String,
    @field:NotBlank
    val street: String,
    @field:NotBlank
    val zip: String,
    @field:NotBlank
    val city: String,
    @field:Email
    val email: String,
)
