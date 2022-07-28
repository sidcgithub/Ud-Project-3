package com.udacity

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    lateinit var viewModel: MainViewModel
    lateinit var dintent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        dintent = Intent(this, DetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        createNotificationChannel()
        registerReceiver(receiver, IntentFilter())

        custom_button.setOnClickListener {
            if (custom_button.buttonState == ButtonState.Loading) {
                Toast.makeText(this, "Download in Progress", Toast.LENGTH_SHORT).show()
            }
            else {
                download()
            }
        }
    }

    fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    1
                )
                false
            }
        } else {
            true
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            this@MainActivity.downloadChecks()


        }
    }

    private fun downloadChecks() {


        viewModel.downloadingCheck()
        viewModel.checkFailure.observe(this, object : Observer<Int> {
            override fun onChanged(t: Int?) {
                if (t != null) {
                    if (t > 0) {
                        val id = downloadID
                        val query: DownloadManager.Query = DownloadManager.Query()
                        query.setFilterById(id)
                        val downloadManager =
                            getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
                        val cursor = downloadManager.query(query)
                        if (cursor.count > 0) {
                            cursor.moveToFirst()
                            when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                                DownloadManager.STATUS_SUCCESSFUL -> {
                                    custom_button.buttonState = ButtonState.Completed
                                    launchNotification(DownloadExecution.SUCCEEDED)
                                    viewModel.checkFailure.postValue(0)
                                }
                                DownloadManager.STATUS_FAILED -> {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Failure!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.checkFailure.postValue(0)
                                    custom_button.buttonState = ButtonState.Completed
                                    launchNotification(DownloadExecution.FAILED)
                                }
                                DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING -> {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Something is wrong!!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    viewModel.checkFailure.postValue(0)
                                    custom_button.buttonState = ButtonState.Completed
                                    launchNotification(DownloadExecution.FAILED)

                                }
                                else -> {
                                    viewModel.downloadingCheck()
                                }
                            }
                        }
                    }
                }
            }

        })

    }

    enum class DownloadExecution {
        SUCCEEDED,
        FAILED
    }

    private fun launchNotification(downloadExecution: DownloadExecution) {
        dintent.putExtra(
            "Status",
            if (downloadExecution == DownloadExecution.SUCCEEDED) "Succeeded" else "Failed"
        )
        pendingIntent = PendingIntent.getActivity(
            this@MainActivity,
            0,
            dintent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder =
            NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(if (downloadExecution == DownloadExecution.SUCCEEDED) "Succeeded" else "Failed")
                .setContentText(
                    if (downloadExecution == DownloadExecution.SUCCEEDED) "Congratulations you have successfully downloaded the repo"
                    else "Sorry something went wrong"
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText( if(downloadExecution == DownloadExecution.SUCCEEDED) "You have downloaded a Github repo. You should now celebrate and rejoice!" else "Failure!")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(
                    R.drawable.ic_launcher_foreground,
                    getString(R.string.open_details),
                    pendingIntent
                )
        builder.setContentTitle( if(downloadExecution == DownloadExecution.SUCCEEDED) "Things have gone smoothly" else "Download is not progressing")

        with(NotificationManagerCompat.from(this@MainActivity)) {
            notify(Random().nextInt(), builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun download() {
        custom_button.buttonState = ButtonState.Clicked
        if (!isStoragePermissionGranted()) {
            custom_button.buttonState = ButtonState.Completed
            return
        }
        when (download_group.checkedRadioButtonId) {
            R.id.glideRadioButton -> downloadGLide()
            R.id.retrofitRadioButton -> downloadRetrofit()
            R.id.loadAppRadioButton -> downloadLoadApp()
            else -> {
                custom_button.buttonState = ButtonState.Completed
                Toast.makeText(this, "Invalid selection", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun downloadLoadApp() {

        Toast.makeText(this, "Downloading Load App repo", Toast.LENGTH_SHORT).show()

        val request =
            DownloadManager.Request(Uri.parse(LoadURL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    getString(R.string.app_name)
                )
        dintent.putExtra("Filename", getString(R.string.app_name))

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        custom_button.buttonState = ButtonState.Loading
        downloadChecks()
    }

    private fun downloadGLide() {
        Toast.makeText(this, "Downloading Glide repo", Toast.LENGTH_SHORT).show()

        val request =
            DownloadManager.Request(Uri.parse(GlideURL))
                .setTitle(getString(R.string.glide_file))
                .setDescription(getString(R.string.glide_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    getString(R.string.glide_file)
                )

        dintent.putExtra("Filename", getString(R.string.glide_file))


        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        custom_button.buttonState = ButtonState.Loading
        downloadChecks()
    }

    private fun downloadRetrofit() {
        Toast.makeText(this, "Downloading Retrofit repo", Toast.LENGTH_SHORT).show()
        val request =
            DownloadManager.Request(Uri.parse(RetrofitURL))
                .setTitle(getString(R.string.retrofit_file))
                .setDescription(getString(R.string.retrofit_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    getString(R.string.retrofit_file)
                )


        dintent.putExtra("Filename", getString(R.string.retrofit_file))
        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
        custom_button.buttonState = ButtonState.Loading
        downloadChecks()
    }

    companion object {
        private const val LoadURL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val GlideURL =
            "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
        private const val RetrofitURL =
            "https://github.com/square/retrofit/archive/refs/heads/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

}
