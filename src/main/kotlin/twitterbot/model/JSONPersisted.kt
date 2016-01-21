package twitterbot.model

import java.io.File

abstract class JSONPersisted(val file: File) {

    init {

        if (!file.exists()) {
            println("$file does not exist -> creating one")
            file.createNewFile()

            reset()
            write()
        } else {
            println("found $file -> reading")
            read()
        }

    }

    abstract fun read()
    abstract fun write()
    abstract fun reset()

}
