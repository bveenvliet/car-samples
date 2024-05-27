package com.example.cargearviewer

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule

import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun mainActivityPropertyChecks() {
        activityScenarioRule.scenario.onActivity {
            assertEquals("com.example.cargearviewer", it.application.packageName)
            assertEquals("com.example.cargearviewer", it.applicationInfo.processName)
            assertEquals("CarGearViewer", it.title)
        }
    }

    @Test
    fun carApiChecks() {
        activityScenarioRule.scenario.onActivity {
            assertTrue(it.car.isConnected)
            assertFalse(it.car.isConnecting)
        }

        // onPause
        activityScenarioRule.scenario.moveToState(Lifecycle.State.STARTED)
        activityScenarioRule.scenario.onActivity {
            assertTrue(it.car.isConnected)
            assertFalse(it.car.isConnecting)
        }
    }

    @Test
    fun onFinishCheck() {
        activityScenarioRule.scenario.onActivity {
            it.finish()
            assertTrue(it.isFinishing)
            Thread.sleep(2000)
        }
        assertEquals(Lifecycle.State.DESTROYED, activityScenarioRule.scenario.state)
    }

}
