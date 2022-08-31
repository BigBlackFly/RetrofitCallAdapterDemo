package martin.zhang.calladaptertest

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import martin.zhang.calladaptertest.login.api.LoginAPI
import martin.zhang.calladaptertest.login.api.LoginInput
import martin.zhang.calladaptertest.login.api.LoginSysInfoInput
import martin.zhang.calladaptertest.network.core.ApiResult
import martin.zhang.calladaptertest.network.core.calladapter.ApiResultCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


class MainActivity : AppCompatActivity() {
    //
    private lateinit var moshi: Moshi
    private lateinit var retrofit: Retrofit
    private lateinit var loginAPI: LoginAPI

    //
    private val tvAccount by lazy { findViewById<EditText>(R.id.tvAccount) }
    private val tvPassword by lazy { findViewById<EditText>(R.id.tvPassword) }
    private val btnLogin by lazy { findViewById<Button>(R.id.btnLogin) }
    private val tvResult by lazy { findViewById<TextView>(R.id.tvResult) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initNetwork()
        btnLogin.setOnClickListener {
            login()
        }
    }

    private fun initNetwork() {
        moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        retrofit = Retrofit.Builder()
            .client(OkHttpClient())
            .baseUrl("https://arpqaf.avon.com/appt/rga/")
            .addCallAdapterFactory(ApiResultCallAdapterFactory())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        loginAPI = retrofit.create(LoginAPI::class.java)
    }

    private fun login() {
        val loginBody = LoginInput(
            device = "mobile",
            password = tvPassword.text.toString(),
            userId = tvAccount.text.toString(),
            mrktCd = "UK",
            langCd = "en",
            appData = LoginSysInfoInput(
                os = "Android",
                osVersion = Build.VERSION.RELEASE,
                deviceId = "",
                appVersion = "",
                deviceName = ""
            )
        )

        CoroutineScope(Dispatchers.Main).launch {
            showLoading()
            when (val result = loginAPI.login(loginBody)) {
                is ApiResult.Success -> {
                    hideLoading("ApiResult.Success")
                    tvResult.text = result.data.toString()
                }
                is ApiResult.Failure -> {
                    hideLoading("ApiResult.Failure")
                    tvResult.text = result.error.toString()
                }
                is ApiResult.Exception -> {
                    hideLoading("ApiResult.Error")
                    tvResult.text = result.exception.toString()
                }
            }
        }
    }

    private fun showLoading() {
        Toast.makeText(this, "loading...", Toast.LENGTH_SHORT).show()
    }

    private fun hideLoading(status: String) {
        Toast.makeText(this, "response is $status", Toast.LENGTH_SHORT).show()
    }
}

