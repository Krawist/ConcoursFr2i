package cm.seeds.concoursfr2i.Activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.navigation.NavController
import androidx.navigation.findNavController
import cm.seeds.concoursfr2i.Helper.REQUEST_CODE_FOR_SCAN_ACTIVITY
import cm.seeds.concoursfr2i.Helper.getDialog
import cm.seeds.concoursfr2i.R
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {

    var navController by Delegates.notNull<NavController>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        initialiseViews()

        addActionsOnView()
    }

    private fun initialiseViews() {
        navController = findNavController(R.id.nav_host_fragment)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode== REQUEST_CODE_FOR_SCAN_ACTIVITY && resultCode== Activity.RESULT_OK){

            val message = if(data?.getStringExtra(Intent.EXTRA_TEXT)!=null) data.getStringExtra(Intent.EXTRA_TEXT) else getString(R.string.scan_impossible)
            val dialog = getDialog(this,getString(R.string.resultats_du_scan),message,"OK","")
            dialog.findViewById<Button>(R.id.dialog_negative_button).visibility= View.GONE
            dialog.findViewById<Button>(R.id.dialog_positive_button).setOnClickListener {
                dialog.dismiss()
            }
            dialog.setCancelable(false)
            dialog.show()

        }else{
            super.onActivityResult(requestCode, resultCode, data)
        }

    }

    private fun addActionsOnView() {
        navController.addOnDestinationChangedListener { _, _, _ ->
            invalidateOptionsMenu()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        if(navController.currentDestination?.id == R.id.informationFragment){
            menu.findItem(R.id.action_see_informations).isVisible = false
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_see_informations ->{
                navController.navigate(R.id.informationFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}