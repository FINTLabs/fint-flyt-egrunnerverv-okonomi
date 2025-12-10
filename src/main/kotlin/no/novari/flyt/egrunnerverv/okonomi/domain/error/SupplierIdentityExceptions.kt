package no.novari.flyt.egrunnerverv.okonomi.domain.error

sealed class DomainValidationException(
    message: String,
) : RuntimeException(message)

class MissingIdentifierException : DomainValidationException("Oppgi enten fødselsnummer eller organisasjonsnummer")

class MultipleIdentifiersException :
    DomainValidationException("Oppgi bare én av delene – fødselsnummer eller organisasjonsnummer")
