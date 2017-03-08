package com.kaczmarkiewiczp.gitcracking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

class NavBarUtils {

    public PrimaryDrawerItem dashboard = new PrimaryDrawerItem()
            .withName("Dashboard")
            .withIcon(R.drawable.ic_home)
            .withIdentifier(1)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public PrimaryDrawerItem repositories = new PrimaryDrawerItem().withName("Repositories")
            .withIcon(R.drawable.ic_git)
            .withIdentifier(2)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public PrimaryDrawerItem issues = new PrimaryDrawerItem()
            .withName("Issues")
            .withIcon(R.drawable.ic_alert_circle_outline)
            .withIdentifier(3)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public PrimaryDrawerItem pullRequests = new PrimaryDrawerItem()
            .withName("Pull Requests")
            .withIcon(R.drawable.ic_source_pull)
            .withIdentifier(4)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public PrimaryDrawerItem people = new PrimaryDrawerItem()
            .withName("People")
            .withIcon(R.drawable.ic_account_multiple)
            .withIdentifier(5)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public PrimaryDrawerItem bookmarks = new PrimaryDrawerItem()
            .withName("Bookmarks")
            .withIcon(R.drawable.ic_star)
            .withIdentifier(6)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public SecondaryDrawerItem settings = new SecondaryDrawerItem()
            .withName("Settings")
            .withIcon(R.drawable.ic_settings)
            .withIdentifier(7)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public SecondaryDrawerItem logout = new SecondaryDrawerItem()
            .withName("Log out")
            .withIdentifier(8)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });

    private Drawer drawer;
    private Activity activity;

    NavBarUtils(Activity activity, Toolbar toolbar, int initialSelection) {
        this.activity = activity;
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
                        settings,
                        logout
                ).withSelectedItem(initialSelection);
        drawer = drawerBuilder.build();
    }

    public Drawer getDrawer() {
        return this.drawer;
    }

    private void drawerItemClicked(View view, int position, IDrawerItem drawerItem) {
        int drawerIdentifier = (int) drawerItem.getIdentifier();

        switch (drawerIdentifier) {
            case 1:
                activity.startActivity(new Intent(activity, Dashboard.class));
                break;
            case 2:
                activity.startActivity(new Intent(activity, Repositories.class));
                break;
            case 8:
                AccountUtils.logout(activity);
                activity.startActivity(new Intent(activity, MainActivity.class));
            default:
                return;
        }
        activity.finish();
    }

    private AccountHeader createAccountHeader(Activity activity) {
        return new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.header2)
                .addProfiles(
                        new ProfileDrawerItem().withName("Patryk").withEmail("kaczmarkiewiczp@mymacewan.ca"),
                        new ProfileSettingDrawerItem().withName("Manage Accounts")
                )
                .withTextColor(Color.BLACK)
                .build();
    }
}
