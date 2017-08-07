package net.donething.autosign.models

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*
import javax.validation.constraints.Size

@Entity
data class Wo17(
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0,
        var uid: Long = 0, // 对应的User的id
        @Size(max = 30) @Column(unique = true)
        var phone: String = "", // 登录手机
        @Size(max = 30) @JsonIgnore
        var password: String = "", // 登录密码
        @Size(max = 20)
        var flow: Double = 0.0, // 已收获的总流量
        var time: String = "", // 最后签到时间
        var success: Boolean = false, // 最后签到结果
        @Column(columnDefinition = "TEXT")
        var msg: String = "", // 最后签到返回的信息
        @Size(max = 20)
        var lastSuccess: String = "", // 最后签到成功的时间
        @JsonIgnore
        var deleted: Boolean = false    // 软删除：只是不显示，而不是真正的删除
)