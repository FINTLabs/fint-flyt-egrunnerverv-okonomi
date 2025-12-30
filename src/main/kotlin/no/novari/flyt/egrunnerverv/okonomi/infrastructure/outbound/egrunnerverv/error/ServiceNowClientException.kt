package no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.egrunnerverv.error

import no.novari.flyt.egrunnerverv.okonomi.domain.model.ServiceNowSysId

sealed class ServiceNowClientException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)

class MissingExternalIdException(
    serviceNowSysId: ServiceNowSysId,
) : ServiceNowClientException(
        "Mangler ekstern leverandør-ID for synkronisering til ServiceNow (sys_id=$serviceNowSysId)",
    )

class ServiceNowPatchConsumerException(
    serviceNowSysId: ServiceNowSysId,
    cause: Throwable? = null,
) : ServiceNowClientException(
        "Kunne ikke oppdatere ServiceNow (sys_id=$serviceNowSysId).",
        cause,
    )
