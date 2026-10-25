package dev.digitalducktape.openride.core.backup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import java.io.File

/**
 * [AutoBackupStore] for Android 7-9 (API 24-28), where [MediaStoreAutoBackupStore]'s
 * `MediaStore.Downloads` / `RELATIVE_PATH` / `IS_PENDING` APIs don't exist. Added for the
 * original Bike's PLTN-RB1VO tablet, which is frozen on Android 7.0.
 *
 * Writes the same file name to the same place, `Download/OpenRide/`, when
 * WRITE_EXTERNAL_STORAGE is granted (it is a runtime permission on these levels; grant it once
 * with `adb shell pm grant <package> android.permission.WRITE_EXTERNAL_STORAGE`). Without the
 * permission it falls back to the app's own external files directory, which needs no
 * permission but is deleted on uninstall.
 */
class LegacyFileAutoBackupStore(context: Context) : AutoBackupStore {
    private val appContext = context.applicationContext
    private val fileName = MediaStoreAutoBackupStore.fileNameFor(appContext.packageName)

    override fun write(json: String): Boolean = runCatching {
        val dir = backupDir() ?: return false
        if (!dir.isDirectory && !dir.mkdirs()) return false
        // Write then rename, so a crash mid-write never leaves a truncated backup behind.
        val tmp = File(dir, "$fileName.tmp")
        tmp.writeText(json, Charsets.UTF_8)
        val target = File(dir, fileName)
        if (target.exists()) target.delete()
        tmp.renameTo(target)
    }.getOrDefault(false)

    override fun readLatest(): String? = runCatching {
        candidateDirs()
            .map { File(it, fileName) }
            .filter { it.isFile }
            .maxByOrNull { it.lastModified() }
            ?.readText(Charsets.UTF_8)
    }.getOrNull()

    private fun backupDir(): File? = if (canWriteSharedStorage()) sharedDir() else privateDir()

    private fun candidateDirs(): List<File> = listOfNotNull(sharedDir(), privateDir())

    @Suppress("DEPRECATION") // getExternalStoragePublicDirectory is the API for these levels.
    private fun sharedDir(): File =
        File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "OpenRide")

    private fun privateDir(): File? = appContext.getExternalFilesDir(null)?.let { File(it, "OpenRide") }

    private fun canWriteSharedStorage(): Boolean =
        appContext.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
}
