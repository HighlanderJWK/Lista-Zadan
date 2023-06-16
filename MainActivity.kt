import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Button
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context

// Struktura danych przechowująca informacje o zadaniu.
data class Task(val description: String, val attachmentUri: Uri?)

class MainActivity : AppCompatActivity() {
    private val tasks = ArrayList<Task>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Adapter do przechowywania i wyświetlania zadań w ListView.
        val adapter = ArrayAdapter<Task>(this, android.R.layout.simple_list_item_1, tasks)
        val listView = findViewById<ListView>(R.id.listView)
        listView.adapter = adapter
        
        val addButton = findViewById<Button>(R.id.addButton)
        val editText = findViewById<EditText>(R.id.editText)

        // Obsługa kliknięcia przycisku Dodaj.
        addButton.setOnClickListener {
            // Otwarcie systemowego dialogu wyboru plików.
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            startActivityForResult(intent, 0)
        }

        // Długie kliknięcie na element listy otwiera opcję udostępniania.
        listView.setOnItemLongClickListener { _, _, position, _ ->
            AlertDialog.Builder(this)
                .setTitle("Udostępnij zadanie")
                .setMessage("Czy chcesz udostępnić to zadanie?")
                .setPositiveButton("Tak") { _, _ ->
                    // Udostępnianie tekstu zadania.
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, tasks[position].description)
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
                .setNegativeButton("Nie", null)
                .show()

            true
        }
    }

    // Odbieranie wyniku z systemowego dialogu wyboru plików.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == RESULT_OK) {
            val uri = data?.data
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, day ->
                    // Wybrano datę, teraz wybieramy czas.
                    val selectedDate = "$day/$month/$year"
                    val timePickerDialog = TimePickerDialog(
                        this,
                        { _, hour, minute ->
                            // Wybrano czas, teraz tworzymy zadanie.
                            val selectedTime = "$hour:$minute"
                            val taskDescription = "${editText.text} (Termin: $selectedDate o godzinie $selectedTime)"
                            tasks.add(Task(taskDescription, uri))
                            adapter.notifyDataSetChanged()
                            editText.text.clear()

                            // Ustawienie przypomnienia.
                            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            val intent = Intent(this, ReminderBroadcast::class.java)
                            intent.putExtra("task", taskDescription)
                            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
                            calendar.set(year, month, day, hour, minute)
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    )
                    timePickerDialog.show()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }
}
