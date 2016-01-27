package twitterbot.logic

import twitterbot.model.Config

class NoiseGenerator(val config: Config) {

    public fun getRandomString(length: Int): String {

        val sb = StringBuffer();

        (0..length).forEach {
            val index = (Math.random() * config.noise_symbols.size)
            sb.append(config.noise_symbols.elementAt(index.toInt()))
        }

        return sb.toString()
    }


    fun getMaybeRandomRetweetReplyWithMaxLength(maxLength: Int): String {
        return getMaybeRandomStringFromSet(maxLength, config.retweet_replies)
    }

    fun getMaybeRandomSignatureWithMaxLength(maxLength: Int): String {
        return getMaybeRandomStringFromSet(maxLength, config.signatures)
    }

    private fun getMaybeRandomStringFromSet(maxLength: Int, from: Set<String>): String {
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
