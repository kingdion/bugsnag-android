package com.bugsnag.android

import com.bugsnag.android.BugsnagTestUtils.generateImmutableConfig

import org.junit.Assert.assertEquals

import org.junit.Test

import java.lang.Thread

class OnErrorTest {

    private val config = generateImmutableConfig()

    @Test
    fun testRunModifiesError() {
        val context = "new-context"

        val onError = OnError {
            it.context = context
            false
        }

        val event = Event.Builder(config, RuntimeException("Test"), null,
            Thread.currentThread(), false, Metadata()
        ).build()
        onError.run(event)
        assertEquals(context, event.context)
    }
}
