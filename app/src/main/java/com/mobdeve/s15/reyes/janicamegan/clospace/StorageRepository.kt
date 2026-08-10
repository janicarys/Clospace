package com.mobdeve.s15.reyes.janicamegan.clospace

import java.io.File
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.storage.storage
import java.util.UUID

class StorageRepository {
    suspend fun uploadClothingImage(file: File): String {
        val userId = SupabaseManager.client.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("No authenticated Supabase user")
        val extension = if (file.name.lowercase().endsWith(".png")) "png" else "jpg"
        val path = "$userId/${UUID.randomUUID()}.$extension"
        SupabaseManager.client.storage.from("clothing-images").upload(path, file.readBytes())
        return SupabaseManager.client.storage.from("clothing-images").publicUrl(path)
    }

    suspend fun deleteClothingImage(path: String) {
        SupabaseManager.client.storage.from("clothing-images").delete(path)
    }
}
