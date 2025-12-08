package no.novari.flyt.egrunnerverv.okonomi.domain.error

sealed class DomainValidationException(
    message: String,
) : RuntimeException(message)

class MissingIdentifierException : DomainValidationException("Fødselsnummer eller orgId må settes")

class MultipleIdentifiersException : DomainValidationException("Kun ett av fødselsnummer/orgnr kan settes")
