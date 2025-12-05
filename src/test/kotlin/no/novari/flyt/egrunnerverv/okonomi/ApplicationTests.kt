package no.novari.flyt.egrunnerverv.okonomi

import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import net.logstash.logback.argument.StructuredArguments.kv
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.logging.toMaskedLogMap
import no.novari.flyt.egrunnerverv.okonomi.adapter.visma.model.StoreResult
import no.novari.flyt.egrunnerverv.okonomi.model.resource.Supplier
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ApplicationTests {
    private val logger: KLogger = KotlinLogging.logger {}

    @Test
    fun contextLoads() {
        val supplier =
            Supplier(
                sysId = "9349daflkjalkd",
                fodselsNummer = "22117949145",
                orgId = "",
                name = "Jan Ove Kongshaug",
                kontoNummer = "123456789",
                street = "Søndersrødsvingen 20",
                zip = "3295",
                city = "Helgeroa",
                email = "jan.ove.kongshaug@gmail.com",
            )

        val result =
            StoreResult(
                company = "29",
                stored = "true",
                updated = "false",
                errors = "false",
            )

        logger.atInfo {
            message = "Leverandør opprettet"
            arguments = arrayOf(kv("leverandør", supplier.toMaskedLogMap()), kv("result", result))
        }
    }
}
