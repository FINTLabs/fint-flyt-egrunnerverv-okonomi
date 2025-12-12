package no.novari.flyt.egrunnerverv.okonomi

import kotlin.test.Test

class ApplicationMainTest {
    @Test
    fun `main starts without exceptions`() {
        val previous = System.getProperty("spring.profiles.active")
        System.setProperty("spring.profiles.active", "test")
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
