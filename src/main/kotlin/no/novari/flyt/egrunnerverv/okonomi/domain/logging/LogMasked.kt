package no.novari.flyt.egrunnerverv.okonomi.domain.logging

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class LogMasked(
    val type: MaskType = MaskType.FULL,
    val first: Int = 0,
    val last: Int = 0,
)

enum class MaskType {
    FULL,
    KEEP_LAST,
    KEEP_FIRST,
    KEEP_FIRST_AND_LAST,
}
