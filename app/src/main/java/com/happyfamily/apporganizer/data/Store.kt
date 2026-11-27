package com.happyfamily.apporganizer.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** 모든 데이터는 기기 안 SharedPreferences 에만 저장 */
class Store(ctx: Context) {
    private val sp = ctx.getSharedPreferences("organizer", Context.MODE_PRIVATE)

    var initialized: Boolean
        get() = sp.getBoolean("init", false)
        set(v) = sp.edit().putBoolean("init", v).apply()

    var excluded: Set<String>
        get() = sp.getStringSet("excluded", emptySet())!!.toSet()
        set(v) = sp.edit().putStringSet("excluded", v).apply()

    var knownPkgs: Set<String>
        get() = sp.getStringSet("known", emptySet())!!.toSet()
        set(v) = sp.edit().putStringSet("known", v).apply()

    var settings: Settings
        get() = Settings(
            speed = sp.getInt("speed", 1),
            minApps = sp.getInt("minApps", 3),
            etcFolder = sp.getBoolean("etcFolder", false),
            homeOnly = sp.getBoolean("homeOnly", true),
            notifyNewApps = sp.getBoolean("notifyNew", false),
        )
        set(s) = sp.edit()
            .putInt("speed", s.speed).putInt("minApps", s.minApps)
            .putBoolean("etcFolder", s.etcFolder).putBoolean("homeOnly", s.homeOnly)
            .putBoolean("notifyNew", s.notifyNewApps).apply()

    var plan: Plan?
        get() = sp.getString("plan", null)?.let { runCatching { planFrom(JSONObject(it)) }.getOrNull() }
        set(p) = sp.edit().putString("plan", p?.let { planTo(it).toString() }).apply()

    var lastScan: ScanResult?
        get() = sp.getString("scan", null)?.let {
            runCatching {
                val o = JSONObject(it)
                ScanResult(o.getLong("t"), o.getInt("p"), o.getInt("l"), o.getInt("f"), o.getJSONArray("h").strings(), o.optString("ln"))
            }.getOrNull()
        }
        set(s) = sp.edit().putString("scan", s?.let {
            JSONObject().put("t", it.time).put("p", it.pages).put("l", it.looseIcons).put("f", it.folders)
                .put("h", JSONArray(it.homePkgs)).put("ln", it.launcher).toString()
        }).apply()

    var lastRun: RunRecord?
        get() = sp.getString("run", null)?.let {
            runCatching {
                val o = JSONObject(it)
                RunRecord(o.getLong("t"), o.getInt("f"), o.getInt("m"), o.getJSONArray("x").strings(), o.optString("n"))
            }.getOrNull()
        }
        set(r) = sp.edit().putString("run", r?.let {
            JSONObject().put("t", it.time).put("f", it.foldersMade).put("m", it.appsMoved)
                .put("x", JSONArray(it.failed)).put("n", it.note).toString()
        }).apply()

    private fun JSONArray.strings() = List(length()) { getString(it) }

    private fun planTo(p: Plan) = JSONObject()
        .put("home", p.fromHomeScan).put("t", p.createdAt)
        .put("u", JSONArray(p.unassigned))
        .put("f", JSONArray().apply {
            p.folders.forEach { f ->
                put(JSONObject().put("id", f.id).put("n", f.name).put("c", f.colorIndex).put("a", JSONArray(f.apps)))
            }
        })

    private fun planFrom(o: JSONObject): Plan {
        val fa = o.getJSONArray("f")
        val folders = List(fa.length()) {
            val f = fa.getJSONObject(it)
            FolderPlan(f.getString("id"), f.getString("n"), f.getInt("c"), f.getJSONArray("a").strings())
        }
        return Plan(folders, o.getJSONArray("u").strings(), o.optBoolean("home"), o.optLong("t"))
    }
}
