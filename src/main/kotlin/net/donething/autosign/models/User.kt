package net.donething.autosign.models

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.Size

@Entity @Table(name = "users")
data class User(
        @Id @GeneratedValue(strategy = GenerationType.AUTO)
        var id: Long = 0,
        @Size(max = 30) @Column(unique = true)
        var username: String = "", // 登录用户名
        @Size(max = 30) @JsonIgnore
        var password: String = "", // 登录密码
        @Size(max = 32) @JsonIgnore // 用户salt
        var salt: String = "",
        @Size(max = 30) @Column(unique = true)
        var email: String = "", // 登录邮箱
        @Size(max = 10)
        var role: String = "", // 用户权限角色
        @Size(max = 20)
        var regTime: String = "", // 注册时间
        @Size(max = 20)
        var logTime: String = "", // 最后登录时间
        @Size(max = 20)
        var logIp: String = "", // 最后登录IP
        @Size(max = 100)
        var token: String = ""  // 用户Token
) : Serializable {
    companion object {
        private val serialVersionUid: Long = 123L
    }
}