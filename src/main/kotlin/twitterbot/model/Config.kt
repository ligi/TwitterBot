package twitterbot.model

import java.util.*

class Config(val oauth_consumer_key: String = "please_enter_key",
             val oauth_consumer_secret: String = "please_enter_secret",
             val target_account: String = "target_account",
             val retweet_accounts: Set<String> = HashSet(),
             val retweet_replies: Set<String> = HashSet(),
             val signatures: Set<String> = HashSet(),
             val min_time_between_checks: Long = 60,
             val max_additional_random_time_between_checks: Long = 180,
             var throw_not_configured: Boolean = true) {


    fun getMaybeRandomRetweetReplyWithMaxLength(maxLength: Int): String {
        return getMaybeRandomString(maxLength,retweet_replies)
    }

    fun getMaybeRandomSignatureWithMaxLength(maxLength: Int): String {
        return getMaybeRandomString(maxLength,signatures)
    }

    private fun getMaybeRandomString(maxLength: Int, from :Set<String>): String {
        val random = (Math.random() * from.size).toInt()
        val randomSignature = from.toList()[random]
        if (randomSignature.length < maxLength) {
            return randomSignature
        }

        from.forEach {
            if (it.length < maxLength)
                return it
        }
        return ""
    }
}
