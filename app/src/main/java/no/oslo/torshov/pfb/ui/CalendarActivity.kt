package no.oslo.torshov.pfb.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import no.oslo.torshov.pfb.R
import no.oslo.torshov.pfb.databinding.ActivityCalendarBinding
import no.oslo.torshov.pfb.ui.fragment.CalendarTabFragment
import no.oslo.torshov.pfb.ui.viewmodel.MainViewModel

class CalendarActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_DATE = "calendar_date"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setTitle(R.string.tab_calendar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.calendarContainer, CalendarTabFragment())
                .commit()
        }

        val date = intent.getStringExtra(EXTRA_DATE)
        if (date != null) {
            binding.root.post {
                androidx.lifecycle.ViewModelProvider(this)[MainViewModel::class.java]
                    .pendingCalendarDate.value = date
            }
        }
    }
}
