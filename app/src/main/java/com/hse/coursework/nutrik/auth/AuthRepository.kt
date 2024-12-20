package com.hse.coursework.nutrik.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.util.Properties
import javax.inject.Inject
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    private val codes = mutableMapOf<String, String>()

    fun sendVerificationCode(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val code = (1000..9999).random().toString()
        codes[email] = code

        try {
            sendEmail(email, "Ваш код подтверждения", "Ваш код подтверждения: $code")
            onSuccess()
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        val username = "nutrik.health.app@gmail.com"
        val password = "3816275papa"

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
        }
        Log.e("Auth", "Sending email to $to; message $body")

        val session = Session.getInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        Thread {
            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(username))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                    setSubject(subject)
                    setText(body)
                }
                Transport.send(message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    fun verifyCode(
        email: String,
        inputCode: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val correctCode = codes[email]
        if (correctCode != null && correctCode == inputCode) {
            codes.remove(email)
            onSuccess()
        } else {
            onFailure(Exception("Неверный код"))
        }
    }

    fun registerOrLogin(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Проверка наличия пользователя в Firebase
        try {

            // Новый пользователь, регистрация
            firebaseAuth.createUserWithEmailAndPassword(email, "default_password")
                .addOnCompleteListener { createTask ->
                    if (createTask.isSuccessful) {
                        onSuccess()
                    } else {
                        Log.d("AuthRepository", "auth ag2")

                        // Пользователь найден, авторизация
                        // Здесь вместо пароля используем код для входа
                        firebaseAuth.signInWithEmailAndPassword(email, "default_password")
                            .addOnCompleteListener { signInTask ->
                                if (signInTask.isSuccessful) {
                                    onSuccess()
                                } else {
                                    onFailure(signInTask.exception ?: Exception("Ошибка авторизации"))
                                }
                            }
                    }
                }
        } catch (e: Exception) {

            Log.d("AuthRepository", "auth ag")

            // Пользователь найден, авторизация
            // Здесь вместо пароля используем код для входа
            firebaseAuth.signInWithEmailAndPassword(email, "default_password")
                .addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {
                        onSuccess()
                    } else {
                        onFailure(signInTask.exception ?: Exception("Ошибка авторизации"))
                    }
                }
        }
    }


    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
}
