package com.example.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

data class AppUpdateInfo(
    val tagName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val publishedAtFormatted: String,
    val apkDownloadUrl: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val isUpdateAvailable: Boolean
) {
    val apkSizeMb: String
        get() = String.format(Locale.getDefault(), "%.1f MB", apkSizeBytes / (1024.0 * 1024.0))
}

sealed class UpdateUiState {
    object Idle : UpdateUiState()
    object Checking : UpdateUiState()
    data class UpdateAvailable(val info: AppUpdateInfo) : UpdateUiState()
    data class UpToDate(val currentVersion: String) : UpdateUiState()
    data class Downloading(val progress: Float, val statusText: String) : UpdateUiState()
    data class ReadyToInstall(val apkFile: File, val info: AppUpdateInfo) : UpdateUiState()
    data class Error(val message: String) : UpdateUiState()
}

object GitHubAppUpdater {

    private const val PREFS_NAME = "github_updater_prefs"
    private const val KEY_REPO_NAME = "github_repo_name"
    const val DEFAULT_REPO = "materossi74/promemoria-farmaci"

    fun getSavedRepo(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REPO_NAME, DEFAULT_REPO) ?: DEFAULT_REPO
    }

    fun saveRepo(context: Context, repo: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_REPO_NAME, repo.trim()).apply()
    }

    suspend fun checkForUpdates(context: Context, repoName: String): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val cleanRepo = repoName.trim().removePrefix("https://github.com/").removeSuffix("/")
            val apiUrl = "https://api.github.com/repos/$cleanRepo/releases/latest"

            val url = URL(apiUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000
                readTimeout = 12000
                setRequestProperty("Accept", "application/vnd.github.v3+json")
                setRequestProperty("User-Agent", "PromemoriaFarmaci-Android-App")
            }

            val responseCode = connection.responseCode
            if (responseCode == 404) {
                return@withContext Result.failure(
                    Exception("Nessuna release trovata nel repository '$cleanRepo'. Assicurati di aver creato una Release pubblica con il file APK allegato.")
                )
            }
            if (responseCode !in 200..299) {
                return@withContext Result.failure(
                    Exception("Errore GitHub (Codice HTTP $responseCode). Verifica il nome del repository e la connessione.")
                )
            }

            val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(jsonText)

            val tagName = root.optString("tag_name", "v1.0")
            val releaseTitle = root.optString("name", tagName)
            val releaseNotes = root.optString("body", "Nessuna nota di rilascio fornita.")
            val publishedAtRaw = root.optString("published_at", "")

            var publishedFormatted = publishedAtRaw
            if (publishedAtRaw.isNotBlank()) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    val date = inputFormat.parse(publishedAtRaw)
                    if (date != null) {
                        publishedFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALIAN).format(date)
                    }
                } catch (e: Exception) {
                    publishedFormatted = publishedAtRaw.take(10)
                }
            }

            // Find APK asset in release
            val assetsArray = root.optJSONArray("assets")
            var apkUrl = ""
            var apkName = "promemoria-farmaci-update.apk"
            var apkSize = 0L

            if (assetsArray != null && assetsArray.length() > 0) {
                for (i in 0 until assetsArray.length()) {
                    val asset = assetsArray.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url", "")
                        apkName = name
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            if (apkUrl.isBlank()) {
                return@withContext Result.failure(
                    Exception("La release '$tagName' è presente su GitHub ma non contiene nessun file con estensione .apk negli asset.")
                )
            }

            val currentVer = BuildConfig.VERSION_NAME
            val isNewer = isNewerVersion(tagName, currentVer)

            val info = AppUpdateInfo(
                tagName = tagName,
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                publishedAtFormatted = publishedFormatted,
                apkDownloadUrl = apkUrl,
                apkFileName = apkName,
                apkSizeBytes = apkSize,
                isUpdateAvailable = isNewer
            )

            Result.success(info)
        } catch (e: Exception) {
            Result.failure(Exception("Impossibile contattare GitHub: ${e.localizedMessage ?: "Verifica la connessione internet"}"))
        }
    }

    fun isNewerVersion(remoteTag: String, currentVersion: String): Boolean {
        try {
            val v1 = remoteTag.removePrefix("v").removePrefix("V").trim().split(".").mapNotNull { it.filter { char -> char.isDigit() }.toIntOrNull() }
            val v2 = currentVersion.removePrefix("v").removePrefix("V").trim().split(".").mapNotNull { it.filter { char -> char.isDigit() }.toIntOrNull() }

            val maxLen = maxOf(v1.size, v2.size)
            for (i in 0 until maxLen) {
                val num1 = v1.getOrElse(i) { 0 }
                val num2 = v2.getOrElse(i) { 0 }
                if (num1 > num2) return true
                if (num1 < num2) return false
            }
            return false
        } catch (e: Exception) {
            return remoteTag != currentVersion
        }
    }

    suspend fun downloadApk(
        context: Context,
        downloadUrl: String,
        fileName: String,
        totalBytesExpected: Long,
        onProgress: (progress: Float, statusText: String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.cacheDir
            if (!downloadDir.exists()) downloadDir.mkdirs()

            val targetFile = File(downloadDir, fileName)
            if (targetFile.exists()) targetFile.delete()

            val url = URL(downloadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 15000
                readTimeout = 30000
                setRequestProperty("User-Agent", "PromemoriaFarmaci-Android-App")
            }

            // Handle GitHub redirect if 302/301
            var finalConnection = connection
            var responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP || responseCode == HttpURLConnection.HTTP_MOVED_PERM) {
                val redirectUrl = connection.getHeaderField("Location")
                finalConnection = (URL(redirectUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", "PromemoriaFarmaci-Android-App")
                }
                responseCode = finalConnection.responseCode
            }

            if (responseCode !in 200..299) {
                return@withContext Result.failure(Exception("Errore durante il download dell'APK (HTTP $responseCode)"))
            }

            val contentLength = if (finalConnection.contentLengthLong > 0) finalConnection.contentLengthLong else totalBytesExpected

            finalConnection.inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val progress = if (contentLength > 0) {
                            (totalRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                        } else 0.5f

                        val currentMb = String.format(Locale.getDefault(), "%.1f", totalRead / (1024.0 * 1024.0))
                        val totalMb = if (contentLength > 0) String.format(Locale.getDefault(), "%.1f MB", contentLength / (1024.0 * 1024.0)) else "dimensione sconosciuta"
                        val percent = (progress * 100).toInt()

                        withContext(Dispatchers.Main) {
                            onProgress(progress, "$percent% ($currentMb / $totalMb)")
                        }
                    }
                    output.flush()
                }
            }

            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(Exception("Download fallito: ${e.localizedMessage ?: "Errore di connessione"}"))
        }
    }

    fun promptInstallApk(context: Context, apkFile: File) {
        if (!apkFile.exists()) return

        // On Android 8.0+, check if unknown app installs are allowed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }
        }

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(installIntent)
    }
}
