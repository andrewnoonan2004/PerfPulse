package com.droid.perfpulse.data

class FakeFileReader(
  private val files: Map<String, String> = emptyMap(),
  private val dirs: Map<String, List<String>> = emptyMap(),
) : SystemFileReader {
  override fun read(path: String): String? = files[path]
  override fun list(path: String): List<String> = dirs[path] ?: emptyList()
}
