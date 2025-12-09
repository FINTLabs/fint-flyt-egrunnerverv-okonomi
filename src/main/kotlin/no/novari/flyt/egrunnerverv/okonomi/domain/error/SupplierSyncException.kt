package no.novari.flyt.egrunnerverv.okonomi.domain.error

sealed class SupplierSyncException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class GenericSupplierException(
    message: String,
    cause: Throwable? = null,
) : SupplierSyncException(message, cause)

class GetSupplierException(
    cause: Throwable? = null,
) : SupplierSyncException("Klarte ikke å hente leverandør", cause)

class CreateSupplierException(
    cause: Throwable? = null,
) : SupplierSyncException("Klarte ikke å opprette leverandør", cause)

class OrganizationToCompanyException(
    cause: Throwable? = null,
) : SupplierSyncException("Klarte ikke å finne firma for tenant", cause)
