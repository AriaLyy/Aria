package com.arialyy.simple.core.download.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.arialyy.simple.R
import com.arialyy.simple.base.BaseActivity
import com.arialyy.simple.databinding.ActivityMultiFragmentActivityBinding

class MultiFragmentActivity : BaseActivity<ActivityMultiFragmentActivityBinding>() {

  private val urls = arrayListOf(
      "http://cpsdown.muzhiwan.com/2020/07/23/com.suyou.xjhx.syad_5f195b471dff6.apk",
      "http://60.174.135.183:88/sdkdown.muzhiwan.com/2020/01/13/com.supercell.clashroyale.ewan.mzw_5e1bc4eb4313e.apk",
      "http://60.174.135.183:88/sdkdown.muzhiwan.com/openfile/2020/08/06/com.maoer.m282_5f2b79232a8b1.apk"
  )
  private val names = arrayListOf(
      "星界幻想",
      "皇室战争",
      "无双帝国"
  )

  override fun setLayoutId(): Int {
    return R.layout.activity_multi_fragment_activity
  }

  override fun init(savedInstanceState: Bundle?) {
    super.init(savedInstanceState)
    val fragmentList = arrayListOf<MultiItemFragment>()
    urls.forEachIndexed { index, url ->
      fragmentList.add(MultiItemFragment(url, names[index]))
    }
    binding.vpPage.adapter = PGAdapter(fragmentList, supportFragmentManager, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
    binding.tabLayout.setupWithViewPager(binding.vpPage)
    binding.tabLayout.getTabAt(0)!!.text = "fm1"
    binding.tabLayout.getTabAt(1)!!.text = "fm2"
    binding.tabLayout.getTabAt(2)!!.text = "fm3"
  }

  private class PGAdapter(
    val fragments: List<MultiItemFragment>,
    fm: FragmentManager,
    state: Int
  ) : FragmentPagerAdapter(fm, state) {
    override fun getItem(position: Int): Fragment {
      return fragments[position]
    }

    override fun getCount(): Int {
      return fragments.size
    }

  }

}