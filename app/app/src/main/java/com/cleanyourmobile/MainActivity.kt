package com.cleanyourmobile

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import rikka.shizuku.Shizuku
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var log: TextView
    private val boxes = mutableListOf<Pair<CheckBox, String>>()
    private val files = listOf("oneplus_safe.txt" to true, "google_optional.txt" to false, "oneplus_caution.txt" to false)

    private fun say(s: String) = runOnUiThread { log.append(s + "\n") }

    private fun sh(cmd: String): String {
        val m = Shizuku::class.java.getDeclaredMethod("newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java)
        m.isAccessible = true
        val p = m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
        val out = p.inputStream.bufferedReader().readText() + p.errorStream.bufferedReader().readText()
        p.waitFor()
        return out.trim()
    }

    private fun ready(): Boolean {
        if (!Shizuku.pingBinder()) { say("Shizuku is not running. Start it first."); return false }
        if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(1); say("Grant the Shizuku permission, then tap again."); return false
        }
        return true
    }

    private fun run(action: (String) -> String) {
        if (!ready()) return
        val sel = boxes.filter { it.first.isChecked }.map { it.second }
        thread { sel.forEach { say("$it -> " + action(it)) }; say("Done.") }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 24, 24, 24) }
        val deb = Button(this).apply { text = "Debloat selected"; setOnClickListener { run { sh("pm uninstall -k --user 0 $it") } } }
        val res = Button(this).apply { text = "Restore selected"; setOnClickListener { run { sh("cmd package install-existing $it") } } }
        val tune = Button(this).apply { text = "Tune (animations, trim caches, AdGuard DNS)"; setOnClickListener {
            if (ready()) thread {
                listOf("settings put global window_animation_scale 0.5", "settings put global transition_animation_scale 0.5",
                    "settings put global animator_duration_scale 0.5", "settings put global private_dns_mode hostname",
                    "settings put global private_dns_specifier dns.adguard-dns.com", "pm trim-caches 999G", "cmd activity kill-all")
                    .forEach { say("$it -> " + sh(it)) }
                say("Done.")
            }
        } }
        log = TextView(this)
        root.addView(deb); root.addView(res); root.addView(tune)
        for ((f, checked) in files) {
            root.addView(TextView(this).apply { text = f; textSize = 16f; setPadding(0, 24, 0, 0) })
            assets.open(f).bufferedReader().readLines().filter { it.isNotBlank() && !it.startsWith("#") }.forEach { line ->
                val pkg = line.substringBefore('#').trim()
                val cb = CheckBox(this).apply { text = line; isChecked = checked }
                boxes += cb to pkg; root.addView(cb)
            }
        }
        root.addView(log)
        setContentView(ScrollView(this).apply { addView(root) })
    }
}
