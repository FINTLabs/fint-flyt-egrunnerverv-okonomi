package no.novari.flyt.egrunnerverv.okonomi.domain.error

sealed class SupplierSyncException(
    message: String,
) : RuntimeException(message)

class GenericSupplierException(
    message: String,
) : SupplierSyncException(message)

class GetSupplierException : SupplierSyncException("Klarte ikke å hente leverandør")

class CreateSupplierException : SupplierSyncException("Klarte ikke å opprette leverandør")

class OrganizationToCompanyException : SupplierSyncException("Klarte ikke å finne firma for tenant")
