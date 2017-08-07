package net.donething.autosign.functions

import net.donething.autosign.models.JSONResult
import net.donething.dtjavatool.HttpTool
import org.junit.Test
import org.springframework.boot.json.BasicJsonParser
import java.io.File

class Wo17SignTest {
    @Test
    fun login() {
        println("Hello.")

        val woSign = wo17.doSign()
        println(woSign)
        /*
        val loginResult = wo17.login("phone", "password")
        println(loginResult)

        val signResult = wo17.sign()
        println(signResult)

        val redPocketResult = wo17.redpocket()
        println(redPocketResult)

        val drawResult = wo17.draw()
        println("抽奖结果：${drawResult.result}")
*/
    }

    @Test
    fun testJsonParser() {
        val jsonText = File("/home/zl/tmp/wo17.json").readText()
        println(jsonText)
        try {
            val jsonResult = jsonParser.parseMap(jsonText)
            println(jsonResult["data"])
            if (jsonResult["code"].toString().toInt() == 0) {
                val signReward = jsonResult["data"] as Map<*, *>
                println(JSONResult(true, 10, signReward["notes"].toString(), signReward["rewardNum"]))
            } else if (jsonResult["code"].toString().toInt() == 1) {
                println(JSONResult(true, 11, "重复签到"))
            } else {
                println(JSONResult(false, 20, jsonResult["msg"].toString()))
            }
        } catch (ex: Exception) {
            println(JSONResult(false, 30, "$ex：$jsonText"))
            ex.printStackTrace()
        }
    }

    val wo17 = Wo17Sign("phone", "password")
    val jsonParser = BasicJsonParser()
}