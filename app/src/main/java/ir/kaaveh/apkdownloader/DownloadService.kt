package ir.kaaveh.apkdownloader

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.*
import java.net.URL
import java.net.URLConnection


class DownloadService : Service() {

    private var notifManager: NotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var url = ""
        if (intent.extras != null) {
            url = intent.extras!!.getString(Url_KEY)!!
        }
        val finalUrl = url
        Thread(Runnable {
            startForeground(NOTIF_ID, getNotification(0))
            download(finalUrl)
            stopForeground(false)
        }).start()
        return START_REDELIVER_INTENT
    }

    private fun download(url: String) {
        try {
            val u = URL(url)
            val conn: URLConnection = u.openConnection()
            val contentLength: Int = conn.contentLength
            val stream = DataInputStream(u.openStream())
            val filePath: String =
                Environment.getExternalStorageDirectory().path.toString() + "/app.apk"
            val f = File(filePath)
            val fos = DataOutputStream(FileOutputStream(f, false))
            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            while (stream.read(data).also { count = it } != -1) {
                total += count.toLong()
                if (contentLength > 0) {
                    val percent = (total * 100 / contentLength).toInt()
                    notifManager!!.notify(NOTIF_ID, getNotification(percent))
                }
                fos.write(data, 0, count)
            }
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getNotification(percent: Int): Notification? {
        return NotificationCompat.Builder(this, CHANEL_ID)
            .setContentTitle("APK Downloader")
            .setContentText("$percent Percent Downloaded!")
            .setSmallIcon(R.drawable.ic_my_download)
            .build()
    }

    companion object {
        const val Url_KEY = "URL_KEY"
        const val NOTIF_ID = 124
        const val CHANEL_ID = "DOWNLOAD_CHANNEL"
    }
}