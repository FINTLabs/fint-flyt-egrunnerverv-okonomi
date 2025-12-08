package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId

sealed class VismaClientException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class VismaGetSupplierException :
    VismaClientException(
        "Klarte ikke å hente leverandør fra Visma",
    )

class VismaCreateSupplierException :
    VismaClientException(
        "Klarte ikke å opprette leverandør i Visma",
    )

class VismaOrganizationToCompanyException(
    tenantId: TenantId,
) : VismaClientException(
        "Klarte ikke å finne firma for tenant $tenantId",
    )
