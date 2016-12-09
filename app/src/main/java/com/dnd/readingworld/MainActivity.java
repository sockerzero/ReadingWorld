package com.dnd.readingworld;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

import com.dnd.readingworld.Adapter.PagerAdapter;
import com.dnd.readingworld.Init.Init;
import com.dnd.readingworld.View.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends ActionBarActivity{

    ViewPager pager;
    TabLayout tabLayout;
    PagerAdapter adapter;
    public FirebaseAuth mFirebaseAuth;
    public FirebaseUser mFirebaseUser;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckLogin();
        if(Init.CheckConnect(this)==false)
        {
            Init.initToast(this,"Plese Connect to Internet");
            finish();
        }

        pager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        FragmentManager manager = getSupportFragmentManager();
        adapter = new PagerAdapter(manager);
        pager.setAdapter(adapter);
        tabLayout.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setTabsFromPagerAdapter(adapter);
    }

    private void CheckLogin(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null){
            startActivity(new Intent(this, Login.class));
        }
    }


}
