package no.novari.flyt.egrunnerverv.okonomi.model.resource

import com.fasterxml.jackson.annotation.JsonProperty

data class Supplier(
    @get:JsonProperty("sys_id")
    val sysId: String,
    // TODO: trolig ikke mulig å populere
    // val number: String,
    @get:JsonProperty("u_fdselsnummer")
    val fodselsNummer: String? = null,
    @get:JsonProperty("u_orgid")
    val orgId: String? = null,
    val name: String,
    @get:JsonProperty("u_kontonummer")
    val kontoNummer: String,
    val street: String,
    val zip: String,
    val city: String,
    // TODO: trolig ikke mulig å populere
    // val country: String,
    // @get:JsonProperty("mobile_phone")
    // TODO: trolig ikke mulig å populere
    // val mobilePhone: String,
    val email: String,
    // TODO: trolig ikke mulig å populere
    // val kommentar: String,
)
