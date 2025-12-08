package no.novari.flyt.egrunnerverv.okonomi.domain.model

import no.novari.flyt.egrunnerverv.okonomi.domain.error.MissingIdentifierException
import no.novari.flyt.egrunnerverv.okonomi.domain.error.MultipleIdentifiersException
import no.novari.flyt.egrunnerverv.okonomi.domain.logging.LogMasked
import no.novari.flyt.egrunnerverv.okonomi.domain.logging.MaskType

sealed interface SupplierIdentity {
    val value: String

    data class Fodselsnummer(
        @LogMasked(type = MaskType.FULL)
        override val value: String,
    ) : SupplierIdentity

    data class OrgId(
        override val value: String,
    ) : SupplierIdentity

    companion object {
        fun from(
            fnr: String?,
            org: String?,
        ): SupplierIdentity {
            return when {
                !fnr.isNullOrBlank() && org.isNullOrBlank() -> Fodselsnummer(fnr.trim())
                fnr.isNullOrBlank() && !org.isNullOrBlank() -> OrgId(org.trim())
                fnr.isNullOrBlank() && org.isNullOrBlank() -> throw MissingIdentifierException()
                else -> throw MultipleIdentifiersException()
            }
        }

        private val fnrRegex = Regex("^\\d{11}$")

        fun fromValue(value: String): SupplierIdentity =
            if (fnrRegex.matches(value.trim())) Fodselsnummer(value.trim()) else OrgId(value.trim())
    }
}
