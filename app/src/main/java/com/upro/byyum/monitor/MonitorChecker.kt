package com.upro.byyum.monitor

import com.upro.byyum.data.entity.MonitorStatus
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

data class CheckResult(
    val status: MonitorStatus,
    val responseTime: Long,
    val message: String
)

object MonitorChecker {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    fun checkHttp(url: String): CheckResult {
        return try {
            val start = System.currentTimeMillis()
            val request = Request.Builder().url(url).get().build()
            val response = httpClient.newCall(request).execute()
            val elapsed = System.currentTimeMillis() - start
            val code = response.code
            response.close()
            when {
                code in 200..299 -> CheckResult(MonitorStatus.HEALTHY, elapsed, "HTTP $code")
                code in 400..499 -> CheckResult(MonitorStatus.ERROR, elapsed, "HTTP $code Client Error")
                code in 500..599 -> CheckResult(MonitorStatus.DOWN, elapsed, "HTTP $code Server Error")
                else -> CheckResult(MonitorStatus.ERROR, elapsed, "HTTP $code")
            }
        } catch (e: Exception) {
            CheckResult(MonitorStatus.DOWN, 0, "Connection failed: ${e.message}")
        }
    }

    fun checkKeyword(url: String, keyword: String): CheckResult {
        return try {
            val start = System.currentTimeMillis()
            val request = Request.Builder().url(url).get().build()
            val response = httpClient.newCall(request).execute()
            val elapsed = System.currentTimeMillis() - start
            val body = response.body?.string() ?: ""
            response.close()
            if (body.contains(keyword, ignoreCase = false)) {
                CheckResult(MonitorStatus.HEALTHY, elapsed, "Keyword found")
            } else {
                CheckResult(MonitorStatus.DOWN, elapsed, "Keyword '$keyword' not found")
            }
        } catch (e: Exception) {
            CheckResult(MonitorStatus.DOWN, 0, "Error: ${e.message}")
        }
    }

    fun checkPing(host: String): CheckResult {
        return try {
            val start = System.currentTimeMillis()
            val reachable = InetAddress.getByName(host).isReachable(5000)
            val elapsed = System.currentTimeMillis() - start
            if (reachable) {
                CheckResult(MonitorStatus.HEALTHY, elapsed, "Ping OK - ${elapsed}ms")
            } else {
                try {
                    val s2 = System.currentTimeMillis()
                    val socket = Socket()
                    socket.connect(InetSocketAddress(host, 80), 5000)
                    val e2 = System.currentTimeMillis() - s2
                    socket.close()
                    CheckResult(MonitorStatus.HEALTHY, e2, "TCP Ping OK - ${e2}ms")
                } catch (ex: Exception) {
                    CheckResult(MonitorStatus.DOWN, elapsed, "Host unreachable")
                }
            }
        } catch (e: Exception) {
            CheckResult(MonitorStatus.DOWN, 0, "Ping error: ${e.message}")
        }
    }

    fun checkPort(host: String, port: Int): CheckResult {
        return try {
            val start = System.currentTimeMillis()
            val socket = Socket()
            socket.connect(InetSocketAddress(host, port), 5000)
            val elapsed = System.currentTimeMillis() - start
            socket.close()
            CheckResult(MonitorStatus.HEALTHY, elapsed, "Port $port is Open")
        } catch (e: Exception) {
            CheckResult(MonitorStatus.DOWN, 0, "Port $port is Closed: ${e.message}")
        }
    }

    fun checkDns(domain: String, expectedIp: String): CheckResult {
        return try {
            val start = System.currentTimeMillis()
            val addresses = InetAddress.getAllByName(domain)
            val elapsed = System.currentTimeMillis() - start
            val resolvedIps = addresses.map { it.hostAddress }
            if (expectedIp.isBlank() || resolvedIps.contains(expectedIp)) {
                CheckResult(MonitorStatus.HEALTHY, elapsed, "DNS OK: ${resolvedIps.joinToString()}")
            } else {
                CheckResult(MonitorStatus.ERROR, elapsed, "DNS mismatch. Expected: $expectedIp, Got: ${resolvedIps.joinToString()}")
            }
        } catch (e: Exception) {
            CheckResult(MonitorStatus.DOWN, 0, "DNS resolution failed: ${e.message}")
        }
    }

    fun checkApi(url: String, jsonKey: String, jsonValue: String): CheckResult {
        return try {
            val start = System.currentTimeMillis()
            val request = Request.Builder().url(url).addHeader("Accept", "application/json").get().build()
            val response = httpClient.newCall(request).execute()
            val elapsed = System.currentTimeMillis() - start
            val body = response.body?.string() ?: ""
            response.close()
            if (jsonKey.isBlank()) {
                try {
                    JSONObject(body)
                    CheckResult(MonitorStatus.HEALTHY, elapsed, "Valid JSON response")
                } catch (e: Exception) {
                    CheckResult(MonitorStatus.ERROR, elapsed, "Invalid JSON response")
                }
            } else {
                try {
                    val json = JSONObject(body)
                    val actualValue = json.optString(jsonKey, "")
                    if (jsonValue.isBlank() || actualValue == jsonValue) {
                        CheckResult(MonitorStatus.HEALTHY, elapsed, "Key '$jsonKey' = '$actualValue'")
                    } else {
                        CheckResult(MonitorStatus.ERROR, elapsed, "Key '$jsonKey' expected '$jsonValue', got '$actualValue'")
                    }
                } catch (e: Exception) {
                    CheckResult(MonitorStatus.ERROR, elapsed, "JSON parse error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            CheckResult(MonitorStatus.DOWN, 0, "API error: ${e.message}")
        }
    }

    fun checkUdp(host: String, port: Int, payload: String): CheckResult {
        return try {
            val start = System.currentTimeMillis()
            val socket = DatagramSocket()
            socket.soTimeout = 5000
            val address = InetAddress.getByName(host)
            val sendData = (if (payload.isBlank()) "ping" else payload).toByteArray()
            val packet = DatagramPacket(sendData, sendData.size, address, port)
            socket.send(packet)
            val elapsed = System.currentTimeMillis() - start
            socket.close()
            CheckResult(MonitorStatus.HEALTHY, elapsed, "UDP packet sent")
        } catch (e: Exception) {
            CheckResult(MonitorStatus.DOWN, 0, "UDP error: ${e.message}")
        }
    }

    fun checkHeartbeat(lastHeartbeat: Long, intervalSeconds: Int): CheckResult {
        val now = System.currentTimeMillis()
        val diff = now - lastHeartbeat
        val intervalMs = intervalSeconds * 1000L
        return if (lastHeartbeat == 0L) {
            CheckResult(MonitorStatus.PENDING, 0, "Waiting for first heartbeat")
        } else if (diff <= intervalMs * 1.5) {
            CheckResult(MonitorStatus.HEALTHY, diff, "Last heartbeat ${diff / 1000}s ago")
        } else {
            CheckResult(MonitorStatus.DOWN, diff, "Heartbeat missed! Last: ${diff / 1000}s ago")
        }
    }
}
