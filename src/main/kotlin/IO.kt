import com.github.ajalt.clikt.core.CliktCommand
import java.io.File
import CommandType.*

/**
 * Stores all possible types of commands that are recognizable. [text] is how each command is invoked
 * through command-line. [requiredArgumentsCnt] is the number of arguments this command requires. [requiresDatabase] is
 * true if the command involves interaction with database; false otherwise.
 */
enum class CommandType(val text: String,
                       val requiredArgumentsCnt: Int,
                       val requiresDatabase: Boolean = true,
                       val helpString: String) {
    OPEN("open", 1, false, "Open database from file located at [ARG1]"),
    NEW("new", 1, false, "Create empty database at [ARG1]"),
    STORE("store", 2, true, "Assign value [ARG2] to [ARG1] key (if either argument contains spaces, use \"\")"),
    GET("get", 1, true, "Get value assigned to [ARG1] key (if key contains spaces, use \"\")"),
    LIST("list", 0, true, "Print all the key value pairs in the database"),
    DELETE("delete", 1, true, "Get value assigned to [ARG1] key (if key contains spaces, use \"\")"),
    SAVE("save", 0, true, "Save database to the file"),
    MOVE("move", 1, true, "Change the destination of saving to [ARG1]. If file exists at [ARG1], save will overwrite it. Otherwise, a new file will be created"),
    CLOSE("close", 0, true, "Save and close database"),
    HELP("help", 0, false, "Print this help"),
    QUIT("quit", 0, false, "Save the database (if opened) and quit the program")
}

/**
 * A class that represents the app. Clikt library magic.
 */
class KVDB: CliktCommand() {
    override fun run() {
        mainLoop()
    }
}

/**
 * Returns true if [fileObject] exists, is a normal file (and not a directory) and is readable; false otherwise.
 */
fun checkFile(fileObject: File): Boolean {
    if (!fileObject.exists()) {
        println("${fileObject.name} does not exist.")
        return false
    }
    if (!fileObject.isFile) {
        println("${fileObject.name} is not a normal file.")
        return false
    }
    if (!fileObject.canRead()) {
        println("${fileObject.name} is not readable.")
        return false
    }

    return true
}

/**
 * Prints nice input prompt: "DB> ", where DB is the name of the file opened if [openedDatabase] is not null
 * and empty otherwise.
 */
fun printPrompt(openedDatabase: Database?) {
    val inputPrompt = if (openedDatabase != null) openedDatabase.fileObject.name else ""
    print("$inputPrompt> ")
}

/**
 * Returns [inputString] parsed into a list of tokens. Tokens can't contain double quotes (""). Each token either contains
 * spaces or does not contain them. The latter type of token should be enclosed in double quotes ("") in [inputString].
 */
fun parseCommand(inputString: String): List<String> {
    return Regex("[^\\s\\t\"]+|\"[^\"]+\"").findAll(inputString).map { it.value.replace("\"", "") }.toList()
}

/**
 * Prints help, which contains information about all possible commands.
 */
fun showHelp() {
    CommandType.values().forEach { command ->
        val arguments = List(command.requiredArgumentsCnt) { "[ARG${it + 1}]" }
        println("${command.text} ${arguments.joinToString(" ", postfix = if (arguments.isNotEmpty()) " " else "")}- ${command.helpString}")
    }
}

/**
 * Compares [command] to all possible commands. Returns null if [command] is null or if there is no such command.
 * Otherwise, returns the type of the command.
 */
fun getCommandType(command: String?): CommandType? {
    if (command == null) {
        return null
    }

    return CommandType.values().find { it.text == command }
}

/**
 * Prints the [value] which is assigned to [key]. If [value] is null then the [key] is not in the database and user
 * must be warned.
 */
fun databaseGetOutput(key: String, value: String?) {
    println(if (value == null) {
        "There is no $key key in the database."
    } else {
        "$value"
    })
}

/**
 * Prints all content of the [database] in ""KEY": "VALUE"" format.
 */
fun databaseListOutput(database: MutableMap<String, String>) {
    database.forEach { (key, value) ->
        println("\"$key\": \"$value\"")
    }
}

/**
 * If the [value] is null then [key] wasn't found in the database and user must be warned.
 */
fun databaseDeleteOutput(key: String, value: String?) {
    if (value == null) {
        println("There is no $key key in the database.")
    }
}

/**
 * The main loop, which includes
 * 1) printing an input prompt
 * 2) getting input from user
 * 3) printing error message if the input was incorrect or performing action in accordance with input
 * 4) go to 1
 *
 * TODO: If the name of the file was provided as program argument, open the database at once
 * TODO: Quiet output mode
 */
fun mainLoop() {
    var openedDatabase: Database? = null

    var inputString: String?

    do {
        printPrompt(openedDatabase)
        inputString = readLine()?.trim()

        if (inputString == null) {
            openedDatabase?.save()
            break
        }

        val parsedCommand = parseCommand(inputString)
        val commandType: CommandType? = getCommandType(parsedCommand.getOrNull(0))

        if (commandType == null) {
            println("Can't recognize command.")
            continue
        }
        if (commandType.requiredArgumentsCnt > parsedCommand.size - 1) {
            println("Too few arguments (required ${commandType.requiredArgumentsCnt}; got ${parsedCommand.size - 1}).")
            continue
        }

        when (commandType) {
            OPEN -> {
                openedDatabase?.save()
                openedDatabase = openDatabase(parsedCommand[1]) ?: openedDatabase
            }
            NEW -> {
                openedDatabase?.save()
                openedDatabase = createDatabase(parsedCommand[1]) ?: openedDatabase
            }
            HELP -> showHelp()
            QUIT -> {
                openedDatabase?.save()
                return
            }
            else -> if (openedDatabase == null) {
                println("Please open the database with \"open <PATH_TO_DATABASE>\" or create new with \"new <PATH_TO_DATABASE>\".")
            }
        }

        if (openedDatabase == null || !commandType.requiresDatabase) {
            continue
        }

        when (commandType) {
            STORE -> openedDatabase.database.set(parsedCommand[1], parsedCommand[2])
            GET -> databaseGetOutput(parsedCommand[1], openedDatabase.database[parsedCommand[1]])
            LIST -> databaseListOutput(openedDatabase.database)
            DELETE -> databaseDeleteOutput(parsedCommand[1], openedDatabase.database.remove(parsedCommand[1]))
            SAVE -> openedDatabase.save()
            MOVE -> openedDatabase.moveTo(parsedCommand[1])
            CLOSE -> {
                openedDatabase.save()
                openedDatabase = null
            }
        }
    } while (inputString != null)
}