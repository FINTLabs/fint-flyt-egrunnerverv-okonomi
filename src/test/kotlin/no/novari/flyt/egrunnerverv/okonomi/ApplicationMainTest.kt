package no.novari.flyt.egrunnerverv.okonomi

import no.novari.main
import kotlin.test.Test

class ApplicationMainTest {
    @Test
    fun `main starts without exceptions`() {
        System.setProperty("spring.profiles.active", "test")
        System.setProperty("novari.kafka.default-replicas", "1")
        val previous = System.getProperty("spring.profiles.active")
        try {
            main(emptyArray())
        } finally {
            if (previous != null) {
                System.setProperty(
                    "spring.profiles.active",
                    previous,
                )
            } else {
                System.clearProperty("spring.profiles.active")
            }
        }
    }
}
