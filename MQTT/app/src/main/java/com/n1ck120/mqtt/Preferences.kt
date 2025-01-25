package com.n1ck120.mqtt

import android.content.Context
import android.content.SharedPreferences


object SharedPreferencesManager {
        private lateinit var sharedPreferences: SharedPreferences

        fun init(context: Context) {
            sharedPreferences = context.getSharedPreferences("config", Context.MODE_PRIVATE)
        }

        fun salvarString(chave: String, valor: String) {
            sharedPreferences.edit().putString(chave, valor).apply()
        }

        fun obterString(chave: String, valorPadrao: String? = null): String? {
            return sharedPreferences.getString(chave, valorPadrao)
        }

        fun chaveExiste(chave: String): Boolean {
            return sharedPreferences.contains(chave)
        }

        fun arquivoTemDados(): Boolean {
            return sharedPreferences.all.isNotEmpty()
        }

        fun removerChave(chave: String) {
            sharedPreferences.edit().remove(chave).apply()
        }

        fun limparTudo() {
            sharedPreferences.edit().clear().apply()
        }
}