package twitterbot.model

import java.util.*

class Config(val oauth_consumer_key: String = "please_enter_key",
             val oauth_consumer_secret: String = "please_enter_secret",
             val target_account: String = "target_account",
             val retweet_accounts: Set<String> = HashSet(),
             val retweet_replies: Set<String> = HashSet(),
             val signatures: Set<String> = HashSet(),
             val noise_symbols: Set<String> = HashSet(),
             val min_time_between_checks: Long = 60,
             val max_additional_random_time_between_checks: Long = 180,
             var throw_not_configured: Boolean = true) {

}
