package com.fakeroot.manager.ui.about

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.fakeroot.manager.R
import java.net.HttpURLConnection
import java.net.URL

class AboutFragment : Fragment() {

    private lateinit var ivAvatar: ImageView
    private lateinit var tvVersion: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivAvatar = view.findViewById(R.id.iv_avatar)
        tvVersion = view.findViewById(R.id.tv_version)

        // Set version
        try {
            val pm = requireContext().packageManager
            val pi = pm.getPackageInfo(requireContext().packageName, 0)
            tvVersion.text = "v${pi.versionName} (${if (android.os.Build.VERSION.SDK_INT >= 28) pi.longVersionCode else pi.versionCode})"
        } catch (e: Exception) {
            tvVersion.text = "v1.0"
        }

        // Load QQ avatar
        loadQQAvatar()
    }

    private fun loadQQAvatar() {
        val qqNumber = getString(R.string.author_qq)
        // Use http to avoid SSL issues on some devices
        val avatarUrl = "http://q1.qlogo.cn/g?b=qq&nk=$qqNumber&s=100"

        Thread {
            var bitmap: Bitmap? = null
            try {
                val url = URL(avatarUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.doInput = true
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Try https as fallback
                try {
                    val httpsUrl = URL("https://q1.qlogo.cn/g?b=qq&nk=$qqNumber&s=100")
                    val conn = httpsUrl.openConnection() as javax.net.ssl.HttpsURLConnection
                    conn.connectTimeout = 10000
                    conn.readTimeout = 10000
                    conn.doInput = true
                    conn.connect()

                    if (conn.responseCode == javax.net.ssl.HttpsURLConnection.HTTP_OK) {
                        val inputStream = conn.inputStream
                        bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream.close()
                    }
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }

            val finalBitmap = bitmap
            Handler(Looper.getMainLooper()).post {
                if (isAdded && finalBitmap != null) {
                    ivAvatar.setImageBitmap(finalBitmap)
                }
            }
        }.start()
    }
}
