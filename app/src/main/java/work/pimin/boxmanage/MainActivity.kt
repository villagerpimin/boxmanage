package work.pimin.boxmanage

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import com.google.android.material.snackbar.Snackbar
import work.pimin.boxmanage.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 右下角点击事件
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "是否刷新?", Snackbar.LENGTH_LONG) // Replace with your own action
                .setAction("确定") {
                    val webContent = binding.root.findViewById<WebView>(R.id.web_content)
                    //webContent.loadUrl("https://www.example.com")
                    webContent.reload()
                }.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            // 查看运行状态
            R.id.action_status -> {
                val result = executeShellCommand("/data/adb/box/scripts/box.service status")
                //showAlert(result)
                true
            }
            // 重启服务
            R.id.action_restart_service -> {
                val result = executeShellCommand("/data/adb/box/scripts/box.service restart")
                //showAlert(result)
                true
            }
            // 重载TProxy服务
            R.id.action_restart_tproxy -> {
                val res = executeShellCommand("/data/adb/box/scripts/box.tproxy restart")
                //showAlert(res)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun executeShellCommand(command: String) {
        // 创建AlertDialog
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("运行结果")
            .setMessage("正在执行命令，请稍侯...")
            .setCancelable(false)
            .setPositiveButton("OK", null)

        val dialog = dialogBuilder.create()
        dialog.show()

        // 启动线程来执行命令
        Thread {
            try {
                val process = ProcessBuilder()
                    .command("su", "-c", command)
                    .redirectErrorStream(true)
                    .start()

                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = StringBuilder()
                var line: String?

                while (reader.readLine().also { line = it } != null) {
                    output.appendLine(line)

                    // 使用Handler更新Dialog的内容
                    runOnUiThread {
                        dialog.setMessage(output.toString()) // 实时更新Dialog内容
                    }
                }

                process.waitFor()
            } catch (e: Throwable) {
                e.printStackTrace()
                runOnUiThread {
                    dialog.setMessage("Error executing command: ${e.message}")
                }
            }
        }.start()
        /*return try {
            val process = ProcessBuilder()
                .command("su", "-c", command) // 使用 su 获取 root 权限
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                output.appendLine(line)
            }

            process.waitFor()
            output.toString() // 返回执行结果
        } catch (e: Throwable) {
            e.printStackTrace()
            "Error executing command: ${e.message}"
        }*/
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(binding.root.context)
            .setTitle("运行结果")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}