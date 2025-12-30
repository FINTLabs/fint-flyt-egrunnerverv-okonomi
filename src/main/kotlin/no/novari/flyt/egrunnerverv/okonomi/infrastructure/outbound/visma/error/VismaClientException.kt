package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId

sealed class VismaClientException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class VismaGetSupplierException(
    tenantId: TenantId,
) : VismaClientException(
        "Kunne ikke hente leverandør i Visma for tenant $tenantId",
    )

class VismaCreateSupplierException(
    tenantId: TenantId,
) : VismaClientException(
        "Kunne ikke opprette leverandør i Visma for tenant $tenantId",
    )

class VismaTenantToCompanyException(
    tenantId: TenantId,
) : VismaClientException(
        "Fant ingen selskaps-mapping for tenant $tenantId",
    )

class VismaIdentifierTooLongException(
    tenantId: TenantId,
) : VismaClientException(
        "Identifikator er for lang for oppslag i Visma for tenant $tenantId",
    )
