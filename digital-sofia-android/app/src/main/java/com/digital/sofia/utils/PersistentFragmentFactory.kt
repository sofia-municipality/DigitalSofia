package com.digital.sofia.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.digital.sofia.ui.fragments.base.BaseFragment
import java.lang.ref.SoftReference

class PersistentFragmentFactory: FragmentFactory() {
    private var persistedFragments = mutableMapOf<String, SoftReference<Fragment?>>()

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        val fragment = super.instantiate(classLoader, className)
        if (fragment is BaseFragment<*, *> && fragment.shouldKeepBinding) {
            if (!persistedFragments.containsKey(className)) {
                persistedFragments[className] = SoftReference(fragment)
            }
            return persistedFragments[className]?.get() ?: fragment
        }
        return fragment
    }
}