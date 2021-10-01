import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.test.*

internal class IOTests {
    private val standardOut = System.out
    private val stream = ByteArrayOutputStream()

    @BeforeTest
    fun setUp() {
        System.setOut(PrintStream(stream))
    }

    @AfterTest
    fun tearDown() {
        System.setOut(standardOut)
    }

    @Test
    fun checkFileTestNormalFile() {
        assertTrue(checkFile(File("src/main/kotlin/main.kt")))
    }

    @Test
    fun checkFileTestNonexistentFile() {
        assertFalse(checkFile(File("i/am/the/nonexistent/file")))
    }

    @Test
    fun checkFileTestDirectory() {
        assertFalse(checkFile(File("src/main/kotlin")))
    }

    @Test
    fun printPromptTestOpenedDatabaseInSameDirectory() {
        printPrompt(Database(File("db.json"), mutableMapOf()), false)
        assertEquals("db.json> ", stream.toString())
    }

    @Test
    fun printPromptTestOpenedDatabaseInAnotherDirectory() {
        printPrompt(Database(File("dir1/dir2/db.json"), mutableMapOf()), false)
        assertEquals("db.json> ", stream.toString())
    }

    @Test
    fun printPromptTestNoDatabaseOpened() {
        printPrompt(null, false)
        assertEquals("> ", stream.toString())
    }

    @Test
    fun printPromptTestQuietModeIsOn() {
        printPrompt(Database(File("db.json"), mutableMapOf()), true)
        assertEquals("", stream.toString())
    }

    @Test
    fun parseCommandTestEmptyStringWithSpacesAndTabs() {
        val answer = listOf<String>()
        assertEquals(answer, parseCommand("   \t  "))
    }

    @Test
    fun parseCommandTestCommandAndPathWithSpaces() {
        val answer = listOf("new", "C:\\Users\\user\\my dbs\\top books.json")
        assertEquals(answer, parseCommand("new  \"C:\\Users\\user\\my dbs\\top books.json\""))
    }

    @Test
    fun parseCommandTestCommandAndTwoSpacelessArguments() {
        val answer = listOf("store", "1", "one")
        assertEquals(answer, parseCommand("store  1   one"))
    }

    @Test
    fun parseCommandTestCommandAndTwoArgumentsWithSpacesAndTabs() {
        val answer = listOf("store", "05 1112", "Masashi\tFujiwara\t27\tEmployed")
        assertEquals(answer, parseCommand("store \"05 1112\" \"Masashi\tFujiwara\t27\tEmployed\""))
    }

    @Test
    fun getCommandTypeTestNoCommand() {
        assertNull(getCommandType(null))
    }

    @Test
    fun getCommandTypeTestWrongCommand() {
        assertNull(getCommandType("nonexistent"))
    }

    @Test
    fun getCommandTypeTestStoreCommand() {
        assertEquals(CommandType.STORE, getCommandType("store"))
    }
}
