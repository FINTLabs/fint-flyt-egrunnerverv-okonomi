package no.novari.flyt.egrunnerverv.okonomi

import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.TenantAdapterProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config.VismaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(
    VismaProperties::class,
    TenantAdapterProperties::class,
)
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
