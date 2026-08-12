package com.mobdeve.s15.reyes.janicamegan.clospace

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseManager {

    val client = createSupabaseClient(

        supabaseUrl = "https://xczzkozcanhfralsqngm.supabase.co",

        supabaseKey = "sb_publishable_gs8Iys2T8cbRqYAk_D86xA_sMxgFQDb"

    ) {

        install(Auth)
        install(Postgrest)
        install(Storage)

    }
}