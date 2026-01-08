package com.zeroscam.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SmokeBootInstrumentedTest {
    @Test
    fun appContext_should_boot_without_crash() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        assertNotNull(ctx)
        assertNotNull(ctx.applicationContext)
    }
}
