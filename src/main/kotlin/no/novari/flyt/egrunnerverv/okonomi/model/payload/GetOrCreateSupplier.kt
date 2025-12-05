package no.novari.flyt.egrunnerverv.okonomi.model.payload

import com.fasterxml.jackson.annotation.JsonProperty

data class GetOrCreateSupplier(
    @get:JsonProperty("sys_id")
    val sysId: String,
    val number: Long,
    @get:JsonProperty("u_fdselsnummer")
    val fodselsNummer: String,
    @get:JsonProperty("u_orgid")
    val orgId: String,
    val name: String,
    @get:JsonProperty("u_kontonummer")
    val kontoNummer: String,
    val street: String,
    val zip: String,
    val city: String,
    val country: String,
    @get:JsonProperty("mobile_phone")
    val mobilePhone: String,
    val email: String,
    val kommentar: String,
)
