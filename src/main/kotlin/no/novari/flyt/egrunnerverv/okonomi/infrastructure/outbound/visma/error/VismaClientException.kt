package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.error

import no.novari.flyt.egrunnerverv.okonomi.domain.model.TenantId

sealed class VismaClientException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class VismaGetSupplierException :
    VismaClientException(
        "Kunne ikke hente leverandør i Visma. Prøv igjen senere eller kontakt FLYT om feilen vedvarer",
    )

class VismaCreateSupplierException :
    VismaClientException(
        "Kunne ikke opprette leverandør i Visma. Prøv igjen senere eller kontakt FLYT om feilen vedvarer",
    )

class VismaTenantToCompanyException(
    tenantId: TenantId,
) : VismaClientException(
        "Fant ingen selskapsmapping for tenant $tenantId",
    )
