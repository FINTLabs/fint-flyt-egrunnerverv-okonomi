package no.novari.flyt.egrunnerverv.okonomi.infrastructure.inbound.web.error

enum class ApiErrorCode(
    val id: Int,
) {
    // Validerings-feil: 1000-1999
    GENERIC_VALIDATION_ERROR(1001),
    MISSING_FODSELSNUMMER_OR_ORGID(1002),
    MULTIPLE_IDENTIFIERS(1003),
    GENERIC_BAD_REQUEST(1004),
    INVALID_TENANT_IDENTIFIER(1005),
    MISSING_ADAPTER_MAPPING(1006),
    MISSING_GATEWAY_BEAN(1007),

    // Supplier-feil: 2000-2999
    GENERIC_SUPPLIER_ERROR(2000),
    GET_SUPPLIER_ERROR(2001),
    CREATE_SUPPLIER_ERROR(2002),
    TENANT_TO_COMPANY_ERROR(2003),

    // Unknown
    UNKNOWN_ERROR(9999),
}
