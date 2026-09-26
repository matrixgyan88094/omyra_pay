package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("OMYRA Pay", appName)
  }

  @Test
  fun `verify semantic version comparison logic`() {
    org.junit.Assert.assertTrue(com.example.updater.UpdateManager.isNewerVersion("1.0.1", "1.0.0"))
    org.junit.Assert.assertTrue(com.example.updater.UpdateManager.isNewerVersion("2.0.0", "1.9.9"))
    org.junit.Assert.assertFalse(com.example.updater.UpdateManager.isNewerVersion("1.0.0", "1.0.0"))
    org.junit.Assert.assertFalse(com.example.updater.UpdateManager.isNewerVersion("0.9.9", "1.0.0"))
  }
}
