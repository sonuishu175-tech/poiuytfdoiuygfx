package com.example

import com.example.data.AppLanguage
import com.example.data.BillingManager
import com.example.data.stringsForLanguage
import com.example.model.AiToolType
import com.example.model.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun all_15_ai_tools_are_configured_with_unique_ids() {
        assertEquals(15, AiToolType.entries.size)
        assertEquals(15, AiToolType.entries.map { it.id }.toSet().size)
        assertTrue(AiToolType.entries.all { it.creditCost >= 1L })
    }

    @Test
    fun localization_switches_between_english_and_hindi() {
        val en = stringsForLanguage("en")
        val hi = stringsForLanguage("hi")
        assertEquals("AI ASSISTANT INDIA", en.appTitle)
        assertEquals("एआई असिस्टेंट इंडिया", hi.appTitle)
        assertEquals(AppLanguage.HINDI, AppLanguage.fromCode("hi"))
        assertEquals(AppLanguage.ENGLISH, AppLanguage.fromCode("unknown"))
    }

    @Test
    fun subscription_plans_and_app_config_defaults_are_valid() {
        assertEquals(3, BillingManager.PLANS.size)
        assertEquals(
            listOf("ai_india_weekly", "ai_india_monthly", "ai_india_yearly"),
            BillingManager.PLANS.map { it.productId }
        )
        val config = AppConfig()
        assertEquals("gemini-3.8-flash", config.activeModel)
        assertFalse(config.maintenanceMode)
    }
}
