package no.novari.flyt.egrunnerverv.okonomi.domain.model

@JvmInline
value class TenantId(
    val id: String,
) {
    init {
        require(id.isNotBlank()) { "Tenant-id kan ikke være tom" }
    }

    override fun toString(): String {
        return id
    }
}
