package com.droid.perfpulse.data

import java.io.File

/**
 * Abstraction over procfs / sysfs so monitors can be unit-tested with fake file trees.
 */
interface SystemFileReader {
  /** Returns trimmed file contents, or null when the file is missing or access is denied. */
  fun read(path: String): String?

  /** Returns child entry names of a directory, or an empty list if unreadable. */
  fun list(path: String): List<String>
}

class FileSystemReader : SystemFileReader {
  override fun read(path: String): String? =
    try {
      File(path).takeIf { it.canRead() }?.readText()?.trim()?.takeIf { it.isNotEmpty() }
    } catch (_: Exception) {
      null
    }

  override fun list(path: String): List<String> =
    try {
      File(path).list()?.toList().orEmpty()
    } catch (_: Exception) {
      emptyList()
    }
}
