package twitterbot.logic

object SymbolPool {
    var SYMBOL_POOL = arrayOf("🙈", "🙊", "🙉", "☮", "🐵", "👀", "👁", "👂", "👅")

    public fun getRandomString(length : Int) : String {

        val sb = StringBuffer();

        (0..length).forEach {
            val index = (Math.random() * SymbolPool.SYMBOL_POOL.size)
            sb.append(SYMBOL_POOL[index.toInt()])
        }

        return sb.toString()
    }
}
