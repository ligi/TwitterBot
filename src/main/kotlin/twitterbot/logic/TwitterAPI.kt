package twitterbot.logic

import twitterbot.model.Config
import twitter4j.TwitterException
import twitter4j.TwitterFactory
import twitter4j.auth.AccessToken
import java.io.BufferedReader
import java.io.InputStreamReader
import twitterbot.model.State

class TwitterAPI {

    val twitter = TwitterFactory.getSingleton()

    constructor(config: Config, persistentState: JSONPersisted<State>) {

        twitter.setOAuthConsumer(config.oauth_consumer_key, config.oauth_consumer_secret)
        val requestToken = twitter.oAuthRequestToken
        var accessToken: AccessToken? = null

        val state = persistentState.get()

        if (state.authToken != null) {
            twitter.oAuthAccessToken = AccessToken(state.authToken, state.authSecret)
        } else {
            val br = BufferedReader(InputStreamReader(System.`in`))
            while (null == accessToken) {
                println("Open the following URL and grant access to your account:")
                println(requestToken.authorizationURL)
                print("Enter the PIN(if available) or just hit enter.[PIN]:")
                val pin = br.readLine()
                try {
                    if (pin.length > 0) {
                        accessToken = twitter.getOAuthAccessToken(requestToken, pin)
                    } else {
                        accessToken = twitter.oAuthAccessToken
                    }
                } catch (te: TwitterException) {
                    if (401 == te.statusCode) {
                        println("Unable to get the access token.")
                    } else {
                        te.printStackTrace()
                    }
                }
            }

            println("Unable to get the access token.")

            state.authSecret = accessToken.tokenSecret
            state.authToken = accessToken.token
            persistentState.write()
        }

    }

}
