package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId

sealed class VismaClientException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class VismaGetSupplierException(
    tenantId: TenantId,
) : VismaClientException(
        "Kunne ikke hente leverandør i Visma for tenant $tenantId. Prøv igjen senere eller kontakt FLYT om feilen vedvarer",
    )

class VismaCreateSupplierException(
    tenantId: TenantId,
) : VismaClientException(
        "Kunne ikke opprette leverandør i Visma for tenant $tenantId. Prøv igjen senere eller kontakt FLYT om feilen vedvarer",
    )

class VismaTenantToCompanyException(
    tenantId: TenantId,
) : VismaClientException(
        "Fant ingen selskapsmapping for tenant $tenantId",
    )

class VismaIdentifierTooLongException(
    tenantId: TenantId,
) : VismaClientException(
        "Identifikator er for lang for Visma-lookup for tenant $tenantId",
    )
