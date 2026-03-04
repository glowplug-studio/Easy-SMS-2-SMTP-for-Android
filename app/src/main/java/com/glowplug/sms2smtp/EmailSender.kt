package com.glowplug.sms2smtp

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object EmailSender {
    fun sendEmailSync(host: String, port: String, user: String, pass: String, from: String, to: String, subject: String, body: String): Pair<Boolean, String?> {
        return try {
            val props = Properties()
            props["mail.smtp.host"] = host
            props["mail.smtp.port"] = port
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.socketFactory.port"] = port
            props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
            
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(user, pass)
                }
            })
            
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(from))
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
            message.subject = subject
            message.setText(body)
            
            Transport.send(message)
            Pair(true, null)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(false, e.message ?: e.toString())
        }
    }
}
