package no.oslo.torshov.pfb.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import no.oslo.torshov.pfb.R
import no.oslo.torshov.pfb.databinding.ActivityMainBinding
import no.oslo.torshov.pfb.ui.adapter.MainPagerAdapter
import no.oslo.torshov.pfb.ui.viewmodel.MainViewModel
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_OPEN_CALENDAR_DATE = "open_calendar_date"
        private const val CALENDAR_TAB_INDEX = 2
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { importFromUri(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val pagerAdapter = MainPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getString(when (position) {
                0 -> R.string.tab_with_thickeners
                else -> R.string.tab_without_thickeners
            })
        }.attach()

        viewModel.loadBundledRecipes()
        binding.fab.setOnClickListener { showAddRecipeDialog() }

        if (savedInstanceState == null) handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val date = intent.getStringExtra(EXTRA_OPEN_CALENDAR_DATE)
        if (date != null) {
            openCalendar(date)
            return
        }
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_export -> { exportRecipesAsJson(); true }
        R.id.action_export_pdf -> { exportRecipesAsPdf(); true }
        R.id.action_import -> {
            importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
            true
        }
        R.id.action_delete_all -> { confirmDeleteAll(); true }
        R.id.action_calendar -> { openCalendar(null); true }
        R.id.action_about -> { showAbout(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun openCalendar(date: String?) {
        val intent = Intent(this, CalendarActivity::class.java)
        if (date != null) intent.putExtra(CalendarActivity.EXTRA_DATE, date)
        startActivity(intent)
    }

    private fun showAbout() {
        val version = packageManager.getPackageInfo(packageName, 0).versionName
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_about)
            .setMessage(getString(R.string.about_message, version))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun confirmDeleteAll() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_all_confirm_title)
            .setMessage(R.string.delete_all_confirm_message)
            .setPositiveButton(R.string.delete) { _, _ -> viewModel.deleteAllRecipes() }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun exportRecipesAsPdf() {
        viewModel.exportRecipesAsPdf { recipes ->
            val file = no.oslo.torshov.pfb.data.repository.RecipePdfExporter.export(this, recipes)
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_subject))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.action_export_pdf)))
        }
    }

    private fun exportRecipesAsJson() {
        viewModel.exportRecipes { json ->
            val file = File(cacheDir, "recipes.json")
            file.writeText(json)
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.export_subject))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, getString(R.string.action_export)))
        }
    }

    private fun importFromUri(uri: Uri) {
        lifecycleScope.launch {
            try {
                val json = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                } ?: return@launch
                viewModel.importRecipes(json) { count ->
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.import_success, count),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (_: Exception) {
                Toast.makeText(this@MainActivity, R.string.import_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun handleIncomingIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.let { importFromUri(it) }
            Intent.ACTION_SEND -> intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                ?.let { importFromUri(it) }
        }
    }

    private fun showAddRecipeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_recipe, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.editRecipeName)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_recipe)
            .setView(dialogView)
            .setPositiveButton(R.string.add) { _, _ ->
                val name = nameInput.text?.toString()?.trim()
                if (!name.isNullOrEmpty()) {
                    viewModel.addRecipe(name)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
