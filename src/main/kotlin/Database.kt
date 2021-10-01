import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

/**
 * Stores the [database] (which is simply a mutable map) and the [fileObject] that was used to originally load it.
 * Database storage format is simply JSON.
 */
data class Database(var fileObject: File, val database: MutableMap<String, String>) {
    /**
     * Changes the saving destination to [newFilename].
     */
    fun moveTo(newFilename: String = fileObject.name) {
        val newFile = File(newFilename)
        if (!newFile.exists()) {
            newFile.createNewFile()
        }

        if (!checkFile(newFile)) {
            return
        }

        fileObject = newFile
    }

    /**
     * Saves the database to [fileObject]. The saving is done in the most dumb way: file is simply overwritten.
     */
    @ExperimentalSerializationApi
    fun save() {
        if (!checkFile(fileObject)) {
            return
        }

        fileObject.outputStream().use { Json.encodeToStream(value=database, it) }
    }
}

/**
 * Loads the database from [filename]. Returns null if file contains malformed data or doesn't exist, is a directory etc.
 */
@ExperimentalSerializationApi
fun openDatabase(filename: String): Database? {
    val fileObject = File(filename)

    if (!checkFile(fileObject)) {
        return null
    }

    val database: MutableMap<String, String>
    try {
        database = fileObject.inputStream().use { Json.decodeFromStream(stream=it) }
    } catch (exception: Exception) {
        // decodeFromStream throws exception if the file is empty, so we have to use this dirty method to check if the file is empty
        if (fileObject.length() > 0) {
            println("${fileObject.name} contains malformed data.")
            return null
        }
        return Database(fileObject, mutableMapOf())
    }
    return Database(fileObject, database)
}

/**
 * Creates a new file and returns empty database. Returns null if file creation caused error.
 */
fun createDatabase(filename: String): Database? {
    val fileObject = File(filename)

    try {
        fileObject.createNewFile()
    } catch (exception: Exception) {
        println("Couldn't create file.")
        return null
    }

    return Database(fileObject, mutableMapOf())
}
