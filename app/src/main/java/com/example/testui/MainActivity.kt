package com.example.testui

import android.content.Intent
import androidx.appcompat.widget.Toolbar
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.testui.activities.ProfileActivity
import com.example.testui.bottom_fragments.AddSelfJobFragment
import com.example.testui.databinding.ActivityMainBinding
import com.example.testui.bottom_fragments.CalendarFragment
import com.example.testui.bottom_fragments.HomeFragment
import com.example.testui.bottom_fragments.ListFragment
import com.example.testui.bottom_fragments.ProfileFragment
import com.example.testui.navigation_fragments.NavFragment1
import com.example.testui.navigation_fragments.NavFragment2
import com.example.testui.navigation_fragments.NavFragment3
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val onBackPressedCallback:OnBackPressedCallback = object : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            onBackPressedAction()
        }

    }

    private fun onBackPressedAction(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            finish()
        }
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var toolbarTitle:TextView


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbarTitle = findViewById(R.id.toolbarTitle)

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false) // Disable default title styling
        }

//        val toolbarTitleText:TextView = findViewById(R.id.toolbarTitle)
//        toolbarTitleText.text = "Home"

        toolbar.inflateMenu(R.menu.main_menu)

        toolbar.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.profile_icon -> {
                    startActivity(Intent(this,ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        val navigationView = findViewById<NavigationView>(R.id.navigation_drawer)
        navigationView.setCheckedItem(R.id.nav_home)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this,drawerLayout,toolbar,R.string.open_drawer,R.string.close_drawer)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Handle BottomNavigationView
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_home -> {
                    replaceFragment(HomeFragment())
                    toolbarTitle.text = "Home"
                }
                R.id.bottom_list -> {
                    replaceFragment(ListFragment())
                    toolbarTitle.text = "Lists"
                }
                R.id.bottom_person -> {
                    replaceFragment(ProfileFragment())
                    toolbarTitle.text = "Profile"
                }
                R.id.bottom_calendar -> {
                    replaceFragment(CalendarFragment())
                    toolbarTitle.text = "Schedule"
                }
                R.id.bottom_add -> {
                    replaceFragment(AddSelfJobFragment())
                    toolbarTitle.text = "Create your own Job"
                }
            }
            true
        }
        bottomNavigationView.selectedItemId = R.id.bottom_home


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true;
    }



    private fun replaceFragment(fragment: Fragment){
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.nav_fragment,fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val itemId = item.itemId

        when (itemId) {
            R.id.nav_home -> {
                replaceFragment(NavFragment1())
                title = "Home"
            }

            R.id.nav_lists -> {
                replaceFragment(NavFragment2())
                title = "Lists"
            }

            R.id.nav_calendar -> {
                replaceFragment(NavFragment3())
                title = "Calendar"
            }

            R.id.nav_profile -> {
                Toast.makeText(this, "Profile fragment selected", Toast.LENGTH_SHORT).show()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)

        return true
    }



}