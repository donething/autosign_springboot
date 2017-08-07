package net.donething.autosign.functions

import net.donething.autosign.models.JSONResult
import net.donething.dtjavatool.HttpTool
import org.slf4j.LoggerFactory
import org.springframework.boot.json.BasicJsonParser

class Wo17Sign(val phone: String, val password: String) {
    /**
     * 一起沃签到、抽奖
     */
    fun doSign(): JSONResult {
        val loginResult = login()
        if (loginResult.success) {
            sign()
            redpocket()
            draw()
            return JSONResult(true, 10, wo17Msg, wo17Flow)
        } else {
            return JSONResult(false, 20, loginResult.msg)
        }
    }

    /**
     * 登录
     */
    fun login(): JSONResult {
        val loginMap = mapOf("logintype" to "passlogin",
                "mobile" to phone,
                "password" to password,
                "checkcode" to "",
                "platform" to "android")
        val loginResponse = httpTool.postResponse(LOGIN_URL, loginMap, REQUEST_HEADER)
        if (loginResponse == null) {
            logger.info("一起沃：$phone:$password ，登录失败！")
            return JSONResult(false, 40, "一起沃登录失败！")
        }
        loginResponse.allHeaders.filter {
            it.name == "Set-Cookie"
        }.forEach {
            loginHeader["Cookie"] = it.value
        }
        val result = httpTool.response2Text(loginResponse)
        wo17Msg.put("登录", result)
        try {
            val jsonResult = jsonParser.parseMap(result)
            if (jsonResult["status"] == "successful") {
                logger.info("$DEBUG_LOG $phone ，登录成功！")
                return JSONResult(true, 10, jsonResult["descri"].toString())
            } else {
                logger.info("$DEBUG_LOG $phone:$password ，登录失败：${jsonResult["descri"]}")
                return JSONResult(false, 20, jsonResult["descri"].toString())
            }
        } catch (ex: Exception) {
            logger.error("$DEBUG_LOG $phone:$password ，登录失败：$ex：$result")
            return JSONResult(false, 30, "$ex：$result")
        }
    }

    /**
     * 签到
     */
    fun sign() {
        val result = httpTool.get(SIGN_URL, REQUEST_HEADER + loginHeader)
        wo17Msg.put("签到", result)
        try {
            val jsonResult = jsonParser.parseMap(result)
            if (jsonResult["code"].toString().toInt() == 0) {
                val signReward = jsonResult["data"] as Map<*, *>
                logger.info("$DEBUG_LOG 签到成功，获得的流量奖励：${signReward["rewardNum"]}")
                wo17Flow += signReward["rewardNum"].toString().toDouble()
            } else if (jsonResult["code"].toString().toInt() == 1) {
                logger.info("$DEBUG_LOG 重复签到！")
            } else {
                logger.info("$DEBUG_LOG 签到失败：$result")
            }
        } catch (ex: Exception) {
            logger.error("$DEBUG_LOG 签到异常：$ex：$result")
        }
    }

    /**
     * 领取红包
     */
    fun redpocket() {
        val result = httpTool.get(REDPOCKET_URL, REQUEST_HEADER + loginHeader)
        wo17Msg.put("红包", result)
        try {
            val jsonResult = jsonParser.parseMap(result)
            if (jsonResult["status"] == "success" && result.contains("恭喜你")) {
                val redPocketReward = jsonResult["data"] as Map<*, *>
                logger.info("$DEBUG_LOG 领取红包成功，获得的流量奖励：${redPocketReward["flow"]}")
                wo17Flow += redPocketReward["flow"].toString().toDouble()
            } else if (jsonResult["status"] == "success" && result.contains("今天已领取")) {
                logger.info("$DEBUG_LOG 重复领取红包！")
            } else {
                logger.info("$DEBUG_LOG 领取红包失败：$result")
            }
        } catch (ex: Exception) {
            logger.error("$DEBUG_LOG 领取红包异常：$ex：$result")
        }
    }

    /**
     * 抽奖
     */
    fun draw() {
        var beforeFlow = wo17Flow
        var result: String
        var jsonResult: Map<String, Any>
        var drawData: Map<*, *>
        while (drawCount() > 0) {
            result = httpTool.get(DRAW_URL, REQUEST_HEADER + loginHeader)
            wo17Msg.put("第N次抽奖", result)
            try {
                jsonResult = jsonParser.parseMap(result)
                if (jsonResult["code"].toString().toInt() == 0) {
                    drawData = jsonResult["data"] as Map<*, *>
                    if (drawData["type"] == "流量") {
                        wo17Flow += drawData["num"].toString().toDouble()
                    }
                } else {
                    logger.info("$DEBUG_LOG 抽奖失败：$result")
                }
            } catch (ex: Exception) {
                logger.error("$DEBUG_LOG 抽奖异常：$ex")
            }
        }
        logger.info("$DEBUG_LOG 抽奖获得的流量奖励：${wo17Flow - beforeFlow}")
    }

    /**
     * 获取可抽奖次数
     */
    fun drawCount(): Int {
        val countResult = httpTool.get(DRAW_COUNT_URL, REQUEST_HEADER + loginHeader)
        try {
            val jsonResult = jsonParser.parseMap(countResult)
            if (jsonResult["code"].toString().toInt() == 0) {
                return jsonResult["data"].toString().toInt()
            } else {
                logger.error("$DEBUG_LOG 获取抽奖次数失败：${jsonResult["msg"]}")
                return 0
            }
        } catch (ex: Exception) {
            logger.error("$DEBUG_LOG 获取抽奖次数异常：${ex.message}")
            return 0
        }
    }

    fun draw17Days(): JSONResult {
        val result = httpTool.get(DRAW17DAYS_URL, REQUEST_HEADER + loginHeader)
        wo17Msg.put("满17天抽奖", result)
        try {
            val jsonResult = jsonParser.parseMap(result)
            return JSONResult()
        } catch (ex: Exception) {
            logger.error("$DEBUG_LOG 满17天抽奖异常：$ex")
            return JSONResult()
        }
    }

    companion object {
        val REQUEST_HEADER = mapOf("User-Agent" to "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36",
                "Host" to "17wo.cn",
                "Referer" to "http://17wo.cn/wowaplogin/login")
        val LOGIN_URL = "http://58.250.151.66/wowap-interface/login/loginPageAction"
        val SIGN_URL = "http://17wo.cn/signin/sign"
        val REDPOCKET_URL = "http://17wo.cn/redPacket/openRedPacket"
        val DRAW_COUNT_URL = "http://17wo.cn/integalPrize/drawCount"
        val DRAW_URL = "http://17wo.cn/integalPrize/draw"
        val DRAW17DAYS_URL = "http://17wo.cn/signDraw/draw"
    }

    private val httpTool = HttpTool()
    private var loginHeader = mutableMapOf<String, String>()        // 因为登录和签到、抽奖等的域名不一样。所需需要保留登录成功后返回的Cookie，签到、抽奖时带上
    private val logger = LoggerFactory.getLogger(this.javaClass)
    private val jsonParser = BasicJsonParser()

    private var wo17Msg = mutableMapOf<String, String>()       // 保存签到信息
    private var wo17Flow = 0.0                                 // 保存签到获取的流量
    private val DEBUG_LOG = "[一起沃]"
}