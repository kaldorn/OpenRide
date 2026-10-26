package dev.digitalducktape.openride.core.backup

/**
 * Where [AutoBackupManager] keeps the rolling automatic backup. One logical slot: writes
 * overwrite the previous automatic backup, reads return the latest one (or `null` if none —
 * fresh device, or a storage location this install can no longer read).
 *
 * An interface so the manager's logic stays plain-JVM testable; production uses
 * [MediaStoreAutoBackupStore] (shared Downloads storage that survives an uninstall).
 */
interface AutoBackupStore {
    /** Overwrites the automatic backup with [json]. Returns `false` if storage refused. */
    fun write(json: String): Boolean

    /** The latest automatic backup's content, or `null` if none is readable. */
    fun readLatest(): String?
}

/**
 * The backup's folder and file name, shared by both stores. Kept outside the API 29
 * [MediaStoreAutoBackupStore] so the Android 7 fork's [LegacyFileAutoBackupStore] can use them.
 */
object AutoBackupFiles {
    const val RELATIVE_PATH = "Download/OpenRide/"

    /** Backup file name for [packageName] — namespaced so the mock and real builds don't collide. */
    fun fileNameFor(packageName: String) = "openride-autobackup-$packageName.json"
}
