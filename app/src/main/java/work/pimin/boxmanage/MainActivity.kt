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
import android.view.MotionEvent
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

        fabBtn()
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
                executeShellCommand("/data/adb/box/scripts/box.service status")
                true
            }
            // 重启核心
            R.id.action_restart_service -> {
                executeShellCommand("/data/adb/box/scripts/box.service restart")
                true
            }
            // 重载TProxy服务
            R.id.action_restart_tproxy -> {
                executeShellCommand("/data/adb/box/scripts/box.tproxy restart")
                true
            }
            // 停止核心
            R.id.action_stop_service -> {
                executeShellCommand("/data/adb/box/scripts/box.service stop")
                true
            }
            // 停止代理
            R.id.action_stop_tproxy -> {
                executeShellCommand("/data/adb/box/scripts/box.tproxy stop")
                true
            }
            /*// 编辑文件
            R.id.action_edit_files -> {
                showAlert("点击了编辑文件")
//                val intent = Intent(this, FileListActivity::class.java)
//                intent.putExtra("FILE_PATH", "/storage/emulated/0/Download")
//                startActivity(intent)
                true
            }
            // 修改代理应用
            R.id.action_edit_app_proxy -> {
                showAlert("修改代理应用")
                true
            }*/
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** 执行命令并弹窗进度 */
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

    /** 右下角操作键处理 */
    private fun fabBtn(){
        val fab = binding.fab

        // 点击刷新
        fab.setOnClickListener { view ->
            Snackbar.make(view, "是否刷新?", Snackbar.LENGTH_LONG) // Replace with your own action
                .setAction("确定") {
                    val webContent = binding.root.findViewById<WebView>(R.id.web_content)
                    if (webContent.url?.contains("127.0.0.1") == true){
                        webContent.reload()
                    } else{
                        webContent.loadUrl(getString(R.string.webui_link))
                    }

                }.show()
        }

        // 可拖动
        var dX = 0f
        var dY = 0f
        var isDragging = false
        val touchSlop = 20 // 设置触摸移动的阈值

        fab.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX + dX
                    val newY = event.rawY + dY

                    // 计算移动的距离
                    if (Math.abs(newX - view.x) > touchSlop || Math.abs(newY - view.y) > touchSlop) {
                        isDragging = true // 更改状态为正在拖动
                    }

                    view.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // 如果没有拖动，则触发点击事件
                        view.performClick()
                    }
                    true
                }
                else -> false
            }
        }
    }
}