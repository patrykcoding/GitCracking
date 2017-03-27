package com.kaczmarkiewiczp.gitcracking;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.IpPrefix;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
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
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class NavBarUtils {

    public static final int DASHBOARD = 1;
    public static final int REPOSITORIES = 2;
    public static final int ISSUES = 3;
    public static final int PULL_REQUESTS = 4;
    public static final int PEOPLE = 5;
    public static final int ACCOUNT_ADD = 6;
    public static final int PROFILE_SETTINGS = 7;

    public PrimaryDrawerItem dashboard = new PrimaryDrawerItem()
            .withName("Dashboard")
            .withIcon(R.drawable.ic_home)
            .withIdentifier(DASHBOARD)
            .withSelectable(false)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public PrimaryDrawerItem repositories = new PrimaryDrawerItem()
            .withName("Repositories")
            .withIcon(R.drawable.ic_repo)
            .withIdentifier(REPOSITORIES)
            .withSelectable(false)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public PrimaryDrawerItem issues = new PrimaryDrawerItem()
            .withName("Issues")
            .withIcon(R.drawable.ic_issue_opened)
            .withIdentifier(ISSUES)
            .withSelectable(false)
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
            .withIdentifier(PULL_REQUESTS)
            .withSelectable(false)
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
            .withIdentifier(PEOPLE)
            .withSelectable(false)
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
            .withSelectable(false)
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
            .withSelectable(false)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public ProfileSettingDrawerItem addAccount = new ProfileSettingDrawerItem()
            .withName("Add account")
            .withIdentifier(ACCOUNT_ADD)
            .withIcon(R.drawable.ic_add_black_24dp)
            .withSelectable(false)
            .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                @Override
                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                    drawerItemClicked(view, position, drawerItem);
                    return false;
                }
            });
    public ProfileSettingDrawerItem profileSettings = new ProfileSettingDrawerItem()
            .withName("Manage Accounts")
            .withIcon(R.drawable.ic_settings)
            .withIdentifier(PROFILE_SETTINGS)
            .withSelectable(false)
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
        AccountHeader accountHeader = createAccountHeader(activity);
        DrawerBuilder drawerBuilder = new DrawerBuilder();
        drawerBuilder.withActivity(activity)
                .withToolbar(toolbar)
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggleAnimated(true)
                .withAccountHeader(accountHeader)
                .addDrawerItems(
                        dashboard,
                        repositories,
                        issues,
                        pullRequests,
                        people,
                        new DividerDrawerItem(),
                        settings,
                        logout
                )
                .withSelectedItem(initialSelection);
        drawer = drawerBuilder.build();
    }

    public void setNavigationDrawerButtonAsUp() {
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        drawer.setOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
            @Override
            public boolean onNavigationClickListener(View clickedView) {
                activity.onBackPressed();
                return true;
            }
        });
    }

    private void drawerItemClicked(View view, int position, IDrawerItem drawerItem) {
        int drawerIdentifier = (int) drawerItem.getIdentifier();
        if (drawer.getCurrentSelectedPosition() == position) {
            return;
        }

        switch (drawerIdentifier) {
            case DASHBOARD:
                activity.startActivity(new Intent(activity, Dashboard.class));
                break;
            case REPOSITORIES:
                activity.startActivity(new Intent(activity, Repositories.class));
                break;
            case ISSUES:
                activity.startActivity(new Intent(activity, Issues.class));
                break;
            case PULL_REQUESTS:
                activity.startActivity(new Intent(activity, PullRequests.class));
                break;
            case PEOPLE:
                activity.startActivity(new Intent(activity, People.class));
                break;
            case 8:
                //AccountUtils.logout(activity);
                activity.startActivity(new Intent(activity, MainActivity.class));
            case PROFILE_SETTINGS:
                activity.startActivity(new Intent(activity, ManageAccounts.class));
            case ACCOUNT_ADD:
                activity.startActivity(new Intent(activity, AddAccount.class));
                return;
            default:
                return;
        }
        activity.finish();
    }

    private AccountHeader createAccountHeader(Activity activity) {
        List<IProfile> profiles = new ArrayList<>();
        Set<String> accounts = AccountUtils.getAccounts(activity);
        String currentUser = AccountUtils.getCurrentUser(activity);
        ProfileDrawerItem activeUser = null;
        Iterator<String> iterator = accounts.iterator();
        while (iterator.hasNext()) {
            String login = iterator.next();
            ProfileDrawerItem profileDrawerItem = new ProfileDrawerItem()
                    .withName(login);
            profiles.add(profileDrawerItem);
            if (currentUser.equals(login)) {
                activeUser = profileDrawerItem;
            }
        }
        profiles.add(addAccount);
        profiles.add(profileSettings);

        AccountHeader accountHeader = new AccountHeaderBuilder()
                .withActivity(activity)
                .withHeaderBackground(R.drawable.header2)
                .withProfiles(profiles)
                .withTextColor(Color.BLACK)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        profileSelected(profile);
                        return false;
                    }
                })
                .build();
        if (activeUser != null) {
            accountHeader.setActiveProfile(activeUser);
        }
        return accountHeader;
    }

    private void profileSelected(IProfile profile) {
        String selectedAccount = profile.getName().toString();
        AccountUtils.setDefaultUser(activity, selectedAccount);
        Intent intent = new Intent(activity, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
    }
}
