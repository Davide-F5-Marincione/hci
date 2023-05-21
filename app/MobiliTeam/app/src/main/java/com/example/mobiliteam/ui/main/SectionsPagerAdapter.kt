package com.example.mobiliteam.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fa: FragmentActivity) :
    FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment.
        if (position == 0) {
            return TravelFragment.newInstance(1)
        } else if (position == 1) {
            return ProfileFragment.newInstance(2)
        }
        return DonationsFragment.newInstance(3)
    }

    override fun getItemCount(): Int {
        return 3
    }
}