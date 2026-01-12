package work.pimin.boxmanage

import android.content.Context
import android.net.http.SslError
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import androidx.core.content.ContextCompat.getSystemService
import work.pimin.boxmanage.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val debugWeb = false

        val webContent = binding.webContent

        webContent.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webContent.settings.apply {
            javaScriptEnabled = true // 启用 JavaScript
            domStorageEnabled = true // // 启用 DOM 存储
            //setSupportZoom(true) // 支持缩放
            builtInZoomControls = true // 允许加载数据 URL
            //loadWithOverviewMode = true // 在加载网页时允许查看宽度
            useWideViewPort = true // 使用广泛的视口
            allowFileAccess = true // 允许本地文件
            cacheMode = WebSettings.LOAD_NO_CACHE // 关闭缓存
            databaseEnabled = true // 启用数据库存储
        }

        // 设置 WebViewClient，确保在内部打开网页
        webContent.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (debugWeb) Log.d("WebView", "Loading URL: ${request?.url}") // 检查是否调用
                view?.loadUrl(request?.url.toString()) // 使用 WebResourceRequest 的 url
                return true // 表示我们处理了这个事件
            }

            // 处理 SSL 错误（仅用于测试）
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed() // 忽略 SSL 错误
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (debugWeb) Log.e("WebView", "Load error: ${error?.description}")
                // 这里可以显示一个错误页面或提示
            }

            override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
                if (debugWeb) Log.e("WebView", "HTTP error: ${errorResponse?.statusCode}")
                // 处理 HTTP 错误
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (debugWeb) Log.d("WebView", "Page finished loading: $url")
            }
        }

        webContent.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                if (debugWeb) Log.d("WebView", "${consoleMessage?.message()} (At ${consoleMessage?.lineNumber()} : ${consoleMessage?.sourceId()})")
                return super.onConsoleMessage(consoleMessage)
            }
        }

        // 清除缓存和历史记录
        webContent.clearCache(true)
        webContent.clearHistory()

        // 加载 URL
        var url: String = "http://127.0.0.1:9090/ui" //  binding.inputUrl.text.toString()
        webContent.loadUrl(url)


        /*binding.inputConform.visibility = View.GONE
        binding.inputUrl.visibility = View.GONE
        binding.inputConform.setOnClickListener {
            url = binding.inputUrl.text.toString() // 读取指定的url, 并加载
            webContent.clearCache(true)
            webContent.clearHistory()
            webContent.loadUrl(url)
        }*/
        /*binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }*/
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}