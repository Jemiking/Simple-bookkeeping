package com.example.myapplication.ui

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.example.myapplication.R
import com.example.myapplication.presentation.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testMainActivityLaunch() {
        // 验证Activity成功启动
        onView(withId(android.R.id.content))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testMainActivityTitle() {
        // 验证标题栏文本
        onView(withId(R.id.toolbar))
            .check(matches(hasDescendant(withText(R.string.app_name))))
    }
} 