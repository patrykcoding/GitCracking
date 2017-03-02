package com.kaczmarkiewiczp.gitcracking;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;

class UiUtils {
    static Drawer setupNavigationDrawer(Activity activity, Toolbar toolbar) {
        DrawerBuilder drawerBuilder = new DrawerBuilder();
        drawerBuilder.withActivity(activity)
                .withToolbar(toolbar)
                .withAccountHeader(createAccountHeader(activity))
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Dashboard").withIcon(activity.getDrawable(R.drawable.ic_home_black_24dp)),
                        new PrimaryDrawerItem().withName("Repositories").withIcon(activity.getDrawable(R.drawable.ic_octicon_repo)),
                        new PrimaryDrawerItem().withName("Issues").withIcon(activity.getDrawable(R.drawable.ic_octicon_issue_24dp)),
                        new PrimaryDrawerItem().withName("Pull Requests").withIcon(activity.getDrawable(R.drawable.ic_octicon_git_pull_request)),
                        new PrimaryDrawerItem().withName("People").withIcon(activity.getDrawable(R.drawable.ic_people_black_24dp)),
                        new PrimaryDrawerItem().withName("Bookmarks").withIcon(activity.getDrawable(R.drawable.ic_star_black_24dp)),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("Settings").withIcon(activity.getDrawable(R.drawable.ic_settings_black_24dp))
                );
        return drawerBuilder.build();
    }

    static private AccountHeader createAccountHeader(Activity activity) {
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(activity)
                .addProfiles(
                        new ProfileDrawerItem().withName("Patryk").withEmail("kaczmarkiewiczp@mymacewan.ca"),
                        new ProfileSettingDrawerItem().withName("Manage Accounts")
                )
                .withTextColor(Color.BLACK)
                .build();
        return accountHeader;
    }
}
