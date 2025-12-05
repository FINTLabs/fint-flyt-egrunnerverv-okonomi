package no.novari.flyt.egrunnerverv.okonomi.model.enum

enum class ApiErrorCode(
    val id: Int,
) {
    // Validerings-feil: 1000-1999
    GENERIC_VALIDATION_ERROR(1001),
    FODSELSNUMMER_OR_ORGID(1002),

    // Visma-feil: 2000-2999
    VISMA_GET_SUPPLIER_ERROR(2001),
    VISMA_CREATE_SUPPLIER_ERROR(2002),
    VISMA_ORGANIZATION_TO_COMPANY_ERROR(2003),

    // Unknown
    UNKNOWN_ERROR(9999),
}
