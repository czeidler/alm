/*
 * Copyright 2015.
 * Distributed under the terms of the GPLv3 License.
 *
 * Authors:
 *      Clemens Zeidler <czei002@aucklanduni.ac.nz>
 */
package nz.ac.auckland.alm.android.test;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import nz.ac.auckland.alm.HorizontalAlignment;
import nz.ac.auckland.alm.VerticalAlignment;
import nz.ac.auckland.alm.XTab;
import nz.ac.auckland.alm.YTab;
import nz.ac.auckland.alm.android.ALMLayout;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new ThreeButtonsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_three_buttons) {
            setFragment(new ThreeButtonsFragment());
            return true;
        }

        if (id == R.id.action_pin_wheel) {
            setFragment(new PinWheelFragment());
            return true;
        }

        if (id == R.id.action_pin_wheel_xml) {
            setFragment(createFragment(R.layout.pin_wheel));
            return true;
        }

        if (id == R.id.action_pin_wheel_tabs_xml) {
            setFragment(createFragment(R.layout.pin_wheel_tabs));
            return true;
        }

        if (id == R.id.action_align_xml) {
            setFragment(createFragment(R.layout.align_grid));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Fragment createFragment(int id) {
        Fragment fragment = new XMLFragment();
        Bundle arguments = new Bundle();
        arguments.putInt("resourceId", id);
        fragment.setArguments(arguments);
        return fragment;
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public static class ThreeButtonsFragment extends Fragment {
        public ThreeButtonsFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Context context = container.getContext();

            ALMLayout almLayout = new ALMLayout(context);
            XTab left = almLayout.getLeftTab();
            YTab top = almLayout.getTopTab();
            XTab right = almLayout.getRightTab();
            YTab bottom = almLayout.getBottomTab();

            XTab x1 = almLayout.addXTab();
            XTab x2 = almLayout.addXTab();

            Button button1 = new Button(context);
            button1.setText("Button 1");
            Button button2 = new Button(context);
            button2.setText("Button 2");
            Button button3 = new Button(context);
            button3.setText("Button 3");

            almLayout.addView(button1, new ALMLayout.LayoutParams(left, top, x1, bottom));
            almLayout.addView(button2, new ALMLayout.LayoutParams(x1, top, x2, bottom));
            almLayout.addView(button3, new ALMLayout.LayoutParams(x2, top, right, bottom));

            almLayout.areaOf(button1).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
            almLayout.areaOf(button2).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
            almLayout.areaOf(button3).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);

            return almLayout;
        }
    }

    public static class PinWheelFragment extends Fragment {
        public PinWheelFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Context context = container.getContext();

            ALMLayout almLayout = new ALMLayout(context);
            XTab left = almLayout.getLeftTab();
            YTab top = almLayout.getTopTab();
            XTab right = almLayout.getRightTab();
            YTab bottom = almLayout.getBottomTab();

            XTab x1 = almLayout.addXTab();
            XTab x2 = almLayout.addXTab();

            YTab y1 = almLayout.addYTab();
            YTab y2 = almLayout.addYTab();

            Button button1 = new Button(context);
            button1.setText("Button 1");
            Button button2 = new Button(context);
            button2.setText("Button 2");
            Button button3 = new Button(context);
            button3.setText("Button 3");
            Button button4 = new Button(context);
            button4.setText("Button 4");
            Button buttonMiddle = new Button(context);
            buttonMiddle.setText("Middle");

            almLayout.addView(button1, new ALMLayout.LayoutParams(left, top, x2, y1));
            almLayout.addView(button2, new ALMLayout.LayoutParams(x2, top, right, y2));
            almLayout.addView(button3, new ALMLayout.LayoutParams(x1, y2, right, bottom));
            almLayout.addView(button4, new ALMLayout.LayoutParams(left, y1, x1, bottom));
            almLayout.addView(buttonMiddle, new ALMLayout.LayoutParams(x1, y1, x2, y2));

            almLayout.areaOf(button1).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
            almLayout.areaOf(button2).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
            almLayout.areaOf(button3).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
            almLayout.areaOf(button4).setAlignment(HorizontalAlignment.FILL, VerticalAlignment.FILL);
            almLayout.areaOf(buttonMiddle).setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

            return almLayout;
        }
    }

    public static class XMLFragment extends Fragment {
        public XMLFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int id = getArguments().getInt("resourceId");
            return inflater.inflate(id, container, false);
        }
    }
}
