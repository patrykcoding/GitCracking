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

    public static PrimaryDrawerItem dashboard = new PrimaryDrawerItem()
            .withName("Dashboard")
            .withIcon(R.drawable.ic_home)
            .withIdentifier(1);
    public static PrimaryDrawerItem repositories = new PrimaryDrawerItem().withName("Repositories")
            .withIcon(R.drawable.ic_git)
            .withIdentifier(2);
    public static PrimaryDrawerItem issues = new PrimaryDrawerItem()
            .withName("Issues")
            .withIcon(R.drawable.ic_alert_circle_outline)
            .withIdentifier(3);
    public  static PrimaryDrawerItem pullRequests = new PrimaryDrawerItem()
            .withName("Pull Requests")
            .withIcon(R.drawable.ic_source_pull)
            .withIdentifier(4);
    public static PrimaryDrawerItem people = new PrimaryDrawerItem()
            .withName("People")
            .withIcon(R.drawable.ic_account_multiple)
            .withIdentifier(5);
    public static PrimaryDrawerItem bookmarks = new PrimaryDrawerItem()
            .withName("Bookmarks")
            .withIcon(R.drawable.ic_star)
            .withIdentifier(6);
    public static SecondaryDrawerItem settings = new SecondaryDrawerItem()
            .withName("Settings")
            .withIcon(R.drawable.ic_settings)
            .withIdentifier(7);


    static Drawer setupNavigationDrawer(Activity activity, Toolbar toolbar) {
        DrawerBuilder drawerBuilder = new DrawerBuilder();
        drawerBuilder.withActivity(activity)
                .withToolbar(toolbar)
                .withAccountHeader(createAccountHeader(activity))
                .addDrawerItems(
                        dashboard,
                        repositories,
                        issues,
                        pullRequests,
                        people,
                        bookmarks,
                        new DividerDrawerItem(),
                        settings
                );
        return drawerBuilder.build();
    }

    static private AccountHeader createAccountHeader(Activity activity) {
        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.header2)
                .addProfiles(
                        new ProfileDrawerItem().withName("Patryk").withEmail("kaczmarkiewiczp@mymacewan.ca"),
                        new ProfileSettingDrawerItem().withName("Manage Accounts")
                )
                .withTextColor(Color.BLACK)
                .build();
        return accountHeader;
    }
}
