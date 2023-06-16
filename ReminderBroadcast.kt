import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Tekst zadania przekazany z MainActivity.
        val task = intent.getStringExtra("task")

        // Utworzenie powiadomienia.
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("reminder", "Reminder", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, "reminder")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Przypomnienie o zadaniu")
            .setContentText(task)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(200, builder.build())
    }
}
