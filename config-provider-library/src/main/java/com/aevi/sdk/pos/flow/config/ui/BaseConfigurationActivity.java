/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.aevi.sdk.pos.flow.config.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.aevi.sdk.pos.flow.config.ConfigComponentProvider;
import com.aevi.sdk.pos.flow.config.R;
import com.aevi.sdk.pos.flow.config.R2;
import com.aevi.sdk.pos.flow.config.flowapps.ProviderAppScanner;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public abstract class BaseConfigurationActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String SAVE_VIEW = "saveView";

    private int lastDisplayItem;
    private Menu appMenu;
    private Fragment currentFragment;

    @Inject
    ProviderAppScanner appEntityScanningHelper;

    @BindView(R2.id.toolbar)
    Toolbar toolbar;

    @BindView(R2.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R2.id.navigation_view)
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ConfigComponentProvider.getFpsConfigComponent().inject(this);

        setContentView(R.layout.activity_configuration);
        ButterKnife.bind(this);
        setupNavigationBar();
        setInitialMenuItem(savedInstanceState);
    }

    protected int getNavigationMenu() {
        return R.menu.settings_menu;
    }

    private void setInitialMenuItem(Bundle savedInstanceState) {
        int defaultMenuItem = getDefaultMenuItem();
        if (savedInstanceState != null) {
            lastDisplayItem = savedInstanceState.getInt(SAVE_VIEW, defaultMenuItem);
        } else {
            lastDisplayItem = defaultMenuItem;
        }
    }

    protected int getDefaultMenuItem() {
        return navigationView.getMenu().getItem(0).getItemId();
    }

    private void setupNavigationBar() {
        navigationView.inflateMenu(getNavigationMenu());
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle actionBarDrawerToggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        super.onDrawerClosed(drawerView);
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        super.onDrawerOpened(drawerView);
                    }
                };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVE_VIEW, lastDisplayItem);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showFragment(lastDisplayItem);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .remove(currentFragment)
                    .commitAllowingStateLoss();
        }
    }

    private void showFragment(int menuId) {
        Fragment fragment = getFragmentForId(menuId);
        FragmentManager fragmentManager = getSupportFragmentManager();
        currentFragment = fragment;
        if (fragmentManager != null) {
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.content, fragment, null);
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            ft.commitAllowingStateLoss();
        }
    }

    protected Fragment getFragmentForId(int menuId) {
        if (menuId == R.id.menu_flow_configuration) {
            setMenuGroupVisible(R.id.flow_config_settings, true);
            toolbar.setTitle(R.string.flow_configuration);
            return new FlowConfigFragment();
        } else if (menuId == R.id.menu_fps_settings) {
            setMenuGroupVisible(R.id.flow_config_settings, false);
            toolbar.setTitle(R.string.fps_settings);
            return new FpsSettingsFragment();
        } else {
            setMenuGroupVisible(R.id.flow_config_settings, false);
            toolbar.setTitle(R.string.appflow_settings);
            return new AppFlowSettingsFragment();
        }
    }

    private void setMenuGroupVisible(int groupId, boolean visible) {
        if (appMenu != null) {
            appMenu.setGroupVisible(groupId, visible);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_menu, menu);
        this.appMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                drawerLayout.closeDrawer(Gravity.LEFT);
            } else {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
            return true;
        } else if (itemId == R.id.settings_flow_menu) {
            startActivity(new Intent(this, FlowConfigFilterActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        lastDisplayItem = item.getItemId();
        new Handler().postDelayed(() -> showFragment(lastDisplayItem), 300);
        drawerLayout.closeDrawers();
        return true;
    }
}
