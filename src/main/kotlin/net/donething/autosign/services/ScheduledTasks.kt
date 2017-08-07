package net.donething.autosign.services

import net.donething.autosign.functions.Wo17Sign
import net.donething.autosign.models.JSONResult
import net.donething.autosign.models.Wo17Repository
import net.donething.dtjavatool.StringTool
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class ScheduledTasks @Autowired constructor(val wo17Repository: Wo17Repository) {
    /**
     * 一起沃定时签到
     */
    @Scheduled(fixedRate = 100000)
    fun wo17Autosign() {
        var wo17Sign: Wo17Sign
        val wo17s = wo17Repository.findAll()
        var curTime = StringTool.date2Str(Date())
        var signResult: JSONResult
        logger.info("========================== $curTime 一起沃自动签到开始 ========================== ")
        wo17s.filter {
            !it.deleted
        }.forEach {
            wo17Sign = Wo17Sign(it.phone, it.password)
            curTime = StringTool.date2Str(Date())
            signResult = wo17Sign.doSign()
            try {
                it.time = curTime
                it.msg = signResult.msg.toString()
                if (signResult.success) {
                    it.flow += signResult.result.toString().toDouble()
                    it.success = true
                    it.lastSuccess = it.time
                }
                wo17Repository.save(it)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        logger.info("========================== 一起沃当天自动签到全部完成 ========================== ")
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)
}