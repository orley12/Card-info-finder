package com.example.cardinfofinder.ui

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasShortClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.cardinfofinder.R
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    var intentRule: IntentsTestRule<MainActivity> = IntentsTestRule(MainActivity::class.java)

//    val scenario = launchActivity<MainActivity>()

    @Test
    fun clicking_scan_with_ocr_display_OcrCaptureActivity() {
        // GIVEN -
        val resultData = Intent()
        val cardNumber = "378282246310005"
        resultData.putExtra("cardNumber", cardNumber)
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)

        // THEN -
        onView(withId(R.id.scan_card_btn)).check(matches(isDisplayed()))
        onView(withId(R.id.scan_card_btn)).perform(click())
        intending(
                allOf(
                        hasComponent(hasShortClassName(".ui.OcrCaptureActivity")),
                        toPackage("com.example.cardinfofinder")
                )
        ).respondWith(result)
//        onView(withId(R.id.card_number_edit_text)).check(matches(withText(cardNumber)))
    }

    @Test
    fun clicking_proceed_to_card_info_displays_detail_card_activity() {
        onView(withId(R.id.card_number_edit_text))
                .perform(typeText("378282246310005"), ViewActions.closeSoftKeyboard())
        onView(withId(R.id.proceed_btn)).perform(click())
        onView(isRoot()).perform(waitFor(20000))

        intended(
                allOf(
                        hasComponent(hasShortClassName(".ui.CardDetailActivity")),
                        toPackage("com.example.cardinfofinder")
                )
        )
        onView(withId(R.id.card_brand)).check(matches(withText("american express")))
        onView(withId(R.id.card_type)).check(matches(withText("credit")))
        onView(withId(R.id.bank)).check(matches(withText("United States of America")))
        onView(withId(R.id.country)).check(matches(withText("AMERICAN EXPRESS COMPANY")))
        onView(withText("Card Brand")).check(matches(isDisplayed()))
        onView(withText("Card Type")).check(matches(isDisplayed()))
        onView(withText("Bank")).check(matches(isDisplayed()))
        onView(withText("Country")).check(matches(isDisplayed()))
        onView(withText("Emoji")).check(matches(isDisplayed()))
    }

    fun waitFor(delay: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isRoot()
            }

            override fun getDescription(): String {
                return "wait for " + delay + "milliseconds"
            }

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadForAtLeast(delay)
            }
        }
    }
}