package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.DEFAULT_PROFILES
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppColorTheme(val key: String, val displayName: String, val primaryHex: Long, val secondaryHex: Long) {
    OCEAN("ocean", "Azul océano", 0xFF0A1F4D, 0xFF1D4FB5),
    ESMERALDA("esmeralda", "Esmeralda", 0xFF0F3D2E, 0xFF1F8F5F),
    ATARDECER("atardecer", "Atardecer", 0xFF7A1F3D, 0xFFE2682E),
    VIOLETA("violeta", "Violeta", 0xFF2E1A6B, 0xFF7B4DE0),
    ROSA("rosa", "Rosa", 0xFFA0245F, 0xFFF06FA5),
    GRAFITO("grafito", "Grafito", 0xFF1C1F26, 0xFF4A5262);

    companion object {
        fun fromKey(key: String): AppColorTheme = entries.find { it.key == key } ?: OCEAN
    }
}

enum class ThemeModeOption(val key: String, val displayName: String) {
    AUTO("auto", "Automático"),
    LIGHT("light", "Claro"),
    DARK("dark", "Oscuro");

    companion object {
        fun fromKey(key: String): ThemeModeOption = entries.find { it.key == key } ?: AUTO
    }
}

enum class SortOption(val key: String, val displayName: String) {
    NAME("name", "Nombre"),
    EXP("exp", "Vencimiento"),
    QTY("qty", "Menos cantidad");

    companion object {
        fun fromKey(key: String): SortOption = entries.find { it.key == key } ?: NAME
    }
}

data class AppConfig(
    val colorTheme: AppColorTheme = AppColorTheme.ESMERALDA,
    val themeMode: ThemeModeOption = ThemeModeOption.AUTO,
    val pantryName: String = "",
    val warningDays: Int = 7,
    val sortOption: SortOption = SortOption.NAME,
    val vibrateOnScan: Boolean = true,
    val diapersPerDay: Int = 6,
    val monthlyBudgetRD: Double = 18000.0,
    val customPrices: Map<String, Double> = emptyMap(),
    // Monetization & Pro limits
    val isProUser: Boolean = false,
    val proPurchaseToken: String = "",
    val proPurchaseTime: Long = 0L,
    val monthlyReceiptScansCount: Int = 0,
    val currentProfileId: String = "p_main",
    val profiles: List<UserProfile> = listOf(DEFAULT_PROFILES.first())
)

object MonetizationConstants {
    const val FREE_PRODUCT_LIMIT = 30
    const val FREE_MONTHLY_RECEIPT_SCANS_LIMIT = 5
    const val PRO_PRICE_RD = "RD$1,690"
    const val PRO_PRODUCT_ID = "despensa_pro_lifetime"
}

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("despensa_prefs", Context.MODE_PRIVATE)

    private val _configFlow = MutableStateFlow(loadConfig())
    val configFlow: StateFlow<AppConfig> = _configFlow.asStateFlow()

    private fun getCurrentYearMonth(): String {
        return SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    }

    private fun loadConfig(): AppConfig {
        val themeKey = prefs.getString("color_theme", AppColorTheme.ESMERALDA.key) ?: AppColorTheme.ESMERALDA.key
        val modeKey = prefs.getString("theme_mode", ThemeModeOption.AUTO.key) ?: ThemeModeOption.AUTO.key
        val pantryName = prefs.getString("pantry_name", "") ?: ""
        val warningDays = prefs.getInt("warning_days", 7)
        val sortKey = prefs.getString("sort_option", SortOption.NAME.key) ?: SortOption.NAME.key
        val vibrate = prefs.getBoolean("vibrate_on_scan", true)
        val ppd = prefs.getInt("diapers_per_day", 6)

        // Pro state
        val isPro = prefs.getBoolean("is_pro_user", false)
        val token = prefs.getString("pro_purchase_token", "") ?: ""
        val purchaseTime = prefs.getLong("pro_purchase_time", 0L)

        // Receipt scan tracking (resets each month)
        val currentMonth = getCurrentYearMonth()
        val storedMonth = prefs.getString("receipt_scans_month", currentMonth) ?: currentMonth
        val scansCount = if (storedMonth == currentMonth) {
            prefs.getInt("receipt_scans_count", 0)
        } else {
            // New month, reset count
            prefs.edit()
                .putString("receipt_scans_month", currentMonth)
                .putInt("receipt_scans_count", 0)
                .apply()
            0
        }

        // Custom Colmado Prices
        val pricesJsonStr = prefs.getString("custom_prices", "{}") ?: "{}"
        val pricesMap = mutableMapOf<String, Double>()
        try {
            val json = JSONObject(pricesJsonStr)
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                pricesMap[k] = json.getDouble(k)
            }
        } catch (_: Exception) {}

        // User Profiles
        val currentProfileId = prefs.getString("current_profile_id", "p_main") ?: "p_main"
        val profilesJsonStr = prefs.getString("user_profiles", "") ?: ""
        val profilesList = mutableListOf<UserProfile>()
        if (profilesJsonStr.isNotBlank()) {
            try {
                val arr = JSONArray(profilesJsonStr)
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val kindStr = obj.optString("kind", "ADULT")
                    val kind = try { com.example.data.model.ProfileKind.valueOf(kindStr) } catch (_: Exception) { com.example.data.model.ProfileKind.ADULT }
                    profilesList.add(
                        UserProfile(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            emoji = obj.optString("emoji", "🏠"),
                            kind = kind,
                            diapersPerDay = obj.optInt("diapersPerDay", 6),
                            diaperSize = obj.optString("diaperSize", "Talla 3")
                        )
                    )
                }
            } catch (_: Exception) {}
        }
        if (profilesList.isEmpty()) {
            profilesList.addAll(DEFAULT_PROFILES)
        }

        val budget = prefs.getFloat("monthly_budget_rd", 18000f).toDouble()

        return AppConfig(
            colorTheme = AppColorTheme.fromKey(themeKey),
            themeMode = ThemeModeOption.fromKey(modeKey),
            pantryName = pantryName,
            warningDays = warningDays,
            sortOption = SortOption.fromKey(sortKey),
            vibrateOnScan = vibrate,
            diapersPerDay = ppd,
            monthlyBudgetRD = budget,
            customPrices = pricesMap,
            isProUser = isPro,
            proPurchaseToken = token,
            proPurchaseTime = purchaseTime,
            monthlyReceiptScansCount = scansCount,
            currentProfileId = currentProfileId,
            profiles = profilesList
        )
    }

    fun setColorTheme(theme: AppColorTheme) {
        prefs.edit().putString("color_theme", theme.key).apply()
        _configFlow.value = _configFlow.value.copy(colorTheme = theme)
    }

    fun setThemeMode(mode: ThemeModeOption) {
        prefs.edit().putString("theme_mode", mode.key).apply()
        _configFlow.value = _configFlow.value.copy(themeMode = mode)
    }

    fun setPantryName(name: String) {
        prefs.edit().putString("pantry_name", name).apply()
        _configFlow.value = _configFlow.value.copy(pantryName = name)
    }

    fun setWarningDays(days: Int) {
        prefs.edit().putInt("warning_days", days).apply()
        _configFlow.value = _configFlow.value.copy(warningDays = days)
    }

    fun setSortOption(sort: SortOption) {
        prefs.edit().putString("sort_option", sort.key).apply()
        _configFlow.value = _configFlow.value.copy(sortOption = sort)
    }

    fun setVibrateOnScan(vibrate: Boolean) {
        prefs.edit().putBoolean("vibrate_on_scan", vibrate).apply()
        _configFlow.value = _configFlow.value.copy(vibrateOnScan = vibrate)
    }

    fun setDiapersPerDay(ppd: Int) {
        prefs.edit().putInt("diapers_per_day", ppd).apply()
        _configFlow.value = _configFlow.value.copy(diapersPerDay = ppd)
    }

    fun setCustomPrice(key: String, price: Double) {
        val current = _configFlow.value.customPrices.toMutableMap()
        current[key] = price
        savePrices(current)
    }

    fun resetPrices() {
        savePrices(emptyMap())
    }

    private fun savePrices(map: Map<String, Double>) {
        val json = JSONObject()
        map.forEach { (k, v) -> json.put(k, v) }
        prefs.edit().putString("custom_prices", json.toString()).apply()
        _configFlow.value = _configFlow.value.copy(customPrices = map)
    }

    // Permanently save Pro status
    fun setProUser(isPro: Boolean, purchaseToken: String = "", purchaseTime: Long = System.currentTimeMillis()) {
        prefs.edit()
            .putBoolean("is_pro_user", isPro)
            .putString("pro_purchase_token", purchaseToken)
            .putLong("pro_purchase_time", if (isPro) purchaseTime else 0L)
            .apply()
        _configFlow.value = _configFlow.value.copy(
            isProUser = isPro,
            proPurchaseToken = purchaseToken,
            proPurchaseTime = if (isPro) purchaseTime else 0L
        )
    }

    // Track receipt scan
    fun incrementReceiptScanCount() {
        val currentMonth = getCurrentYearMonth()
        val storedMonth = prefs.getString("receipt_scans_month", currentMonth) ?: currentMonth
        val currentCount = if (storedMonth == currentMonth) {
            prefs.getInt("receipt_scans_count", 0)
        } else 0
        val newCount = currentCount + 1

        prefs.edit()
            .putString("receipt_scans_month", currentMonth)
            .putInt("receipt_scans_count", newCount)
            .apply()

        _configFlow.value = _configFlow.value.copy(
            monthlyReceiptScansCount = newCount
        )
    }

    // Profiles
    fun setCurrentProfile(profileId: String) {
        prefs.edit().putString("current_profile_id", profileId).apply()
        _configFlow.value = _configFlow.value.copy(currentProfileId = profileId)
    }

    fun addProfile(profile: UserProfile) {
        val list = _configFlow.value.profiles.toMutableList()
        if (list.none { it.id == profile.id }) {
            list.add(profile)
            saveProfiles(list)
        }
    }

    fun setMonthlyBudget(budget: Double) {
        prefs.edit().putFloat("monthly_budget_rd", budget.toFloat()).apply()
        _configFlow.value = _configFlow.value.copy(monthlyBudgetRD = budget)
    }

    private fun saveProfiles(list: List<UserProfile>) {
        val arr = JSONArray()
        list.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("emoji", p.emoji)
                put("kind", p.kind.name)
                put("diapersPerDay", p.diapersPerDay)
                put("diaperSize", p.diaperSize)
            }
            arr.put(obj)
        }
        prefs.edit().putString("user_profiles", arr.toString()).apply()
        _configFlow.value = _configFlow.value.copy(profiles = list)
    }
}
