package org.effervescence.app19.ca.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.effervescence.app19.ca.fragments.LoginFragment
import org.effervescence.app19.ca.fragments.SignupFragment
import org.effervescence.app19.ca.fragments.UserDetailsInputFragment

class LoginSignupFragmentPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> LoginFragment()
            1 -> SignupFragment()
            else -> UserDetailsInputFragment()
        }
    }

    override fun getCount(): Int {
        return 3
    }

}