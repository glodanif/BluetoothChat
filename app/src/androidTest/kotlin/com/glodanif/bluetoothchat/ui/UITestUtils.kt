package com.glodanif.bluetoothchat.ui

import androidx.test.espresso.matcher.BoundedMatcher
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.View
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class UITestUtils {

    companion object {

        fun atPosition(position: Int, itemMatcher: Matcher<View>): Matcher<View> {

            checkNotNull(itemMatcher)

            return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {

                override fun describeTo(description: Description) {
                    description.appendText("has item at position $position: ")
                    itemMatcher.describeTo(description)
                }

                override fun matchesSafely(view: RecyclerView): Boolean {
                    val viewHolder = view.findViewHolderForAdapterPosition(position)
                            ?: // has no item on such position
                            return false
                    return itemMatcher.matches(viewHolder.itemView)
                }
            }
        }

        fun withToolbarSubTitle(title: CharSequence): Matcher<View> {
            return withToolbarSubTitle(Matchers.`is`(title))
        }

        fun withToolbarSubTitle(textMatcher: Matcher<CharSequence>): Matcher<View> {

            return object : BoundedMatcher<View, Toolbar>(Toolbar::class.java) {

                public override fun matchesSafely(toolbar: Toolbar): Boolean {
                    return textMatcher.matches(toolbar.subtitle)
                }

                override fun describeTo(description: Description) {
                    description.appendText("with toolbar title: ")
                    textMatcher.describeTo(description)
                }
            }
        }
    }
}
