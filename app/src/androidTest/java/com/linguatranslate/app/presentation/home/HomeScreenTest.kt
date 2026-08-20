package com.linguatranslate.app.presentation.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.linguatranslate.app.presentation.theme.LinguaTranslateTheme
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI test for the Home screen. Exercises source/target
 * language selectors, text input, the Translate button, and the
 * microphone button per spec section 28.
 *
 * Note: this test renders HomeScreen with a real HiltViewModel, so it
 * must run as an androidTest under Hilt's testing setup with a test
 * NetworkModule/DatabaseModule providing fakes. See README for the
 * @HiltAndroidTest / CustomTestRunner wiring required to execute this.
 */
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun sourceAndTargetLanguageSelectorsAreDisplayed() {
        composeTestRule.setContent {
            LinguaTranslateTheme { HomeScreen() }
        }

        composeTestRule.onNodeWithText("FROM").assertIsDisplayed()
        composeTestRule.onNodeWithText("TO").assertIsDisplayed()
    }

    @Test
    fun typingInInputFieldUpdatesText() {
        composeTestRule.setContent {
            LinguaTranslateTheme { HomeScreen() }
        }

        composeTestRule.onNodeWithText("Type or speak…").performTextInput("Good morning")
    }

    @Test
    fun translateButtonIsDisplayedAndClickable() {
        composeTestRule.setContent {
            LinguaTranslateTheme { HomeScreen() }
        }

        composeTestRule.onNodeWithText("Translate").assertIsDisplayed().performClick()
    }

    @Test
    fun microphoneButtonIsDisplayed() {
        composeTestRule.setContent {
            LinguaTranslateTheme { HomeScreen() }
        }

        composeTestRule.onNodeWithContentDescription("Speak input").assertIsDisplayed()
    }

    @Test
    fun swapLanguagesButtonIsDisplayed() {
        composeTestRule.setContent {
            LinguaTranslateTheme { HomeScreen() }
        }

        composeTestRule.onNodeWithContentDescription("Swap languages").assertIsDisplayed()
    }
}
