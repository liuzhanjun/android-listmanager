package com.iceheart.listmanager.tasklistpager;

import android.support.v4.view.ViewPager;

import com.iceheart.listmanager.MainActivity;

/**
* Created by nmasse on 11/19/13.
*/
public class TaskListPagerOnPageChangeListener implements ViewPager.OnPageChangeListener {

    private MainActivity mainActivity;
    private final TaskListPagerAdapter pagerAdapter;

    public TaskListPagerOnPageChangeListener(MainActivity mainActivity, TaskListPagerAdapter pagerAdapter) {
        this.mainActivity = mainActivity;
        this.pagerAdapter = pagerAdapter;
    }

    @Override
    public void onPageSelected(int position) {
        mainActivity.setTitle(pagerAdapter.getMainPageTitle(position));
        mainActivity.setShareContent(pagerAdapter.getTaskListForPage(position));
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {}

    @Override
    public void onPageScrollStateChanged(int arg0) {}
}
