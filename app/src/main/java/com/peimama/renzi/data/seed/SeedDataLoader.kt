package com.peimama.renzi.data.seed

import android.content.Context
import kotlinx.serialization.json.Json

class SeedDataLoader(
    private val context: Context,
) {
    private val parser = Json {
        ignoreUnknownKeys = true
    }

    fun load(): SeedRoot {
        val json = context.assets.open("sample_words.json")
            .bufferedReader()
            .use { it.readText() }
        return parser.decodeFromString<SeedRoot>(json)
    }
}
