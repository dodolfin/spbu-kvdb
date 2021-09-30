import com.github.ajalt.clikt.core.CliktCommand
import java.io.File
import CommandType.*

/**
 * Stores all possible types of commands that are recognizable. [text] is how each command is invoked
 * through command-line. [requiredArgumentsCnt] is the number of arguments this command requires. [requiresDatabase] is
 * true if the command involves interaction with database; false otherwise.
 */
enum class CommandType(val text: String, val requiredArgumentsCnt: Int, val requiresDatabase: Boolean = true) {
    OPEN("open", 1, false),
    NEW("new", 1, false),
    STORE("store", 2),
    GET("get", 1),
    LIST("list", 0),
    DELETE("delete", 1),
    SAVE("save", 0),
    MOVE("move", 1),
    CLOSE("close", 0),
    HELP("help", 0, false),
    QUIT("quit", 0, false)
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
    TODO()
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
 * The main loop, which includes
 * 1) printing an input prompt
 * 2) getting input from user
 * 3) printing error message if the input was incorrect or performing action in accordance with input
 * 4) go to 1
 *
 * TODO: If the name of the file was provided as program argument, open the database at once
 * TODO: Don't print input prompt if file is provided as input (e.g. user wrote some script for automation)
 * TODO: Quiet output mode
 */
fun mainLoop() {
    var openedDatabase: Database? = null

    var inputString: String?

    do {
        printPrompt(openedDatabase)
        inputString = readLine()?.trim()

        if (inputString == null) {
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
        if (commandType.requiresDatabase && openedDatabase == null) {
            println("Please open the database with \"open <PATH_TO_DATABASE>\" or create new with \"new <PATH_TO_DATABASE>\".")
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
            STORE -> openedDatabase?.store(parsedCommand[1], parsedCommand[2])
            GET -> openedDatabase?.get(parsedCommand[1])
            LIST -> openedDatabase?.list()
            DELETE -> openedDatabase?.delete(parsedCommand[1])
            SAVE -> openedDatabase?.save()
            MOVE -> openedDatabase?.moveTo(parsedCommand[1])
            CLOSE -> {
                openedDatabase?.save()
                openedDatabase = null
            }
            HELP -> showHelp()
            QUIT -> return
        }
    } while (inputString != null)
}