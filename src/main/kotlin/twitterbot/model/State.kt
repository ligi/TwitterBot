package twitterbot.model

import java.util.*

open class State(var authToken: String? = null,
                 var authSecret: String? = null,
                 var next_follower_get_cursor: Long = -1,
                 var unprocessed_follower_pool: Set<String> = HashSet(),
                 var processed_follower_pool: Set<String> = HashSet(),
                 var seen_tweets_to_retweet_count: Map<String, Int> = HashMap(),
                 var last_seen_tweet_date: Long = Date().time,
                 var processed_retweeters: Set<String> = HashSet()) {

    fun maybeUpdateLastSeenTweetDate(date: Date): Boolean {
        if (date.after(Date(last_seen_tweet_date))) {
            last_seen_tweet_date = date.time
            return true
        }
        return false
    }
}
