package com.mobdeve.s15.reyes.janicamegan.clospace

import io.github.jan.supabase.postgrest.from

class ClothingRepository {

    suspend fun addClothing(item: Clothing) {
        SupabaseManager.client
            .from("clothing")
            .insert(item)
    }

    suspend fun getWardrobe(): List<Clothing> {
        return SupabaseManager.client
            .from("clothing")
            .select()
            .decodeList<Clothing>()
    }

    suspend fun deleteClothing(id: Long) {
        SupabaseManager.client
            .from("clothing")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}