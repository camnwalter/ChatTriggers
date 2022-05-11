package com.chattriggers.ctjs.browser

import com.chattriggers.ctjs.CTJS
import com.chattriggers.ctjs.browser.pages.LoginPage
import com.chattriggers.ctjs.utils.kotlin.fromJson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object WebsiteAPI {
    fun login(username: String, password: String): WebsiteOwner? {
        val (code, res) = sendFormUrlEncodedRequest("${CTJS.WEBSITE_ROOT}/api/account/login") {
            put("username", username)
            put("password", password)
        }

        if (code != 200)
            return null

        return try {
            CTJS.gson.fromJson<WebsiteOwner>(res)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createAccount(username: String, email: String, password: String): WebsiteOwner? {
        val (code) = sendFormUrlEncodedRequest("${CTJS.WEBSITE_ROOT}/api/account/new") {
            put("username", username)
            put("email", email)
            put("password", password)
        }

        return if (code == 200) login(username, password) else null
    }

    fun logout() {
        val url = "${CTJS.WEBSITE_ROOT}/api/account/logout"

        URL(url).openConnection().apply {
            setRequestProperty("User-Agent", "Mozilla/5.0")
        }.getInputStream().bufferedReader().readText()

        ModuleBrowser.username.set(null)
        ModuleBrowser.id.set(null)
        ModuleBrowser.rank.set(null)

        ModuleBrowser.isLoggedIn = false
        LoginPage.clearInputs()
        ModuleBrowser.showPage(ModuleBrowser.Page.Account)
    }

    fun getUserModules(id: Int, offset: Int): WebsiteResponse? {
        val url = "${CTJS.WEBSITE_ROOT}/api/modules?owner=$id&offset=${offset * 10}"

        return try {
            val response = URL(url).openConnection().apply {
                setRequestProperty("User-Agent", "Mozilla/5.0")
            }.getInputStream().bufferedReader().readText()

            CTJS.gson.fromJson(response)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun sendFormUrlEncodedRequest(url: String, builder: PostRequestBuilder.() -> Unit): Response {
        val prb = PostRequestBuilder()
        prb.builder()
        val urlData = prb.urlString

        val connection: HttpURLConnection = (URL(url).openConnection() as HttpURLConnection).apply {
            setRequestProperty("User-Agent", "Mozilla/5.0")
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 5000
            readTimeout = 5000
        }

        try {
            connection.connect()

            OutputStreamWriter(connection.outputStream).use {
                it.write(urlData)
                it.flush()
            }

            if (connection.responseCode != 200)
                return Response(connection.responseCode, "")

            val text = BufferedReader(InputStreamReader(connection.inputStream)).use {
                it.readText()
            }

            return Response(200, text)
        } catch (e: Exception) {
            e.printStackTrace()
            return Response(500, "")
        } finally {
            connection.disconnect()
        }
    }

    private data class Response(val code: Int, val text: String)

    class PostRequestBuilder {
        var urlString = ""

        fun put(key: String, value: String) {
            val encodedKey = URLEncoder.encode(key, "UTF-8")
            val encodedValue = URLEncoder.encode(value, "UTF-8")

            if (urlString == "") {
                urlString = "$encodedKey=$encodedValue"
            } else {
                urlString += "&$encodedKey=$encodedValue"
            }
        }
    }
}