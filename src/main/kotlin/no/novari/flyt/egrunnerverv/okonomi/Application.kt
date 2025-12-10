package no.novari.flyt.egrunnerverv.okonomi

import no.novari.flyt.egrunnerverv.okonomi.infrastructure.config.AdapterLeverandorProperties
import no.novari.flyt.egrunnerverv.okonomi.infrastructure.outbound.visma.config.VismaProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableConfigurationProperties(
    VismaProperties::class,
    AdapterLeverandorProperties::class,
)
@SpringBootApplication
@EnableRetry
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
