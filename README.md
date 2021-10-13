# Курс основ программирования на МКН СПбГУ
## Проект 2: key-value база данных

[Постановка задачи](./TASK.md)

### Usage
`distribution/bin/pf-2021-kvdb [OPTIONS] [DATABASETOOPEN]` (run from root directory)

Console utility kvdb provides command-line interface to work with simple key-value
database (which internally is JSON).

### Interface
Once launched, you get into interactive shell. Enter your command and press Enter to
evaluate it. Enter `...> help` to get the list of commands with explanations.

### Input
To open a database, you either have to provide DATABASETOOPEN argument like this: `java -jar kvdb db.json`,
or enter `...> open db.json` while in the interactive shell. If database was loaded successfully,
the input prompt will change to `db.json> `.

### Input from file
If you have a script (sequence of commands) to edit some database, use `java -jar kvdb -q < script.txt`. 
In this case you'll find OPTION `-q` or `--quiet`
useful. This option suppresses input prompt `...> `, which is printed incorrectly if commands are read from file.

### Output
The format of output is human-readable. Further details are provided in the Commands overview section.

### Commands overview

|Command|Explanation|
|-------|-----------|
|`open [ARG1]`|Open database from file located at `[ARG1]`. Error messages: `<FILE> does not exist.`, `<FILE> is not a normal file.`, `<FILE> is not readable.`, `<FILE> contains malformed data`.|
|`new [ARG1]`|Create empty database at `[ARG1]`. Error messages: `Couldn't create file`.|
|`store [ARG1] [ARG2]`|Assign value `[ARG2]` to `[ARG1]` key (if either argument contains spaces, use ""). Note that if key `[ARG1]` is present in the database, the previous value is overwritten.|
|`get [ARG1]`|Get value assigned to `[ARG1]` key (if key contains spaces, use ""). Error messages: `There is no <KEY> key in the database.`.|
|`list`|Print all the key value pairs in the database. Format of output: each pair is printed on separate line in following format:`"<KEY>": "<VALUE>"`.|
|`delete [ARG1]`|Get value assigned to `[ARG1]` key (if key contains spaces, use ""). Error messages: `There is no <KEY> key in the database.`.|
|`save`|Save database to the file (by overwriting it).|
|`move [ARG1]`|Change the destination of saving to `[ARG1]`. If file exists at path `[ARG1]`, save will overwrite it. Otherwise, a new file will be created.|
|`close`|Save and close database.|
|`help`|Print list of all available commands with explanations.|
|`quit`|Save the database (if opened) and quit the program.|

### Getting help
Use `-h` or `--help` option to get information about OPTIONS and DATABASETOOPEN. Use `...> help` inside interactive
shell to show list of available commands.

### Testing
All files related to external testing are now in an `ext_tests` directory.
Run `ext_tests/test.sh script.txt` to run a simple script (saved in `ext_tests/script.txt`) that contains few operations with database.
There is also `ext_tests/big_script.txt`, that contains 1000 operations with database:
1) create a new database, 
2) insert a key and value, 
3) close database, 
4) reopen it, 
5) repeat from 2

This script is generated with `ext_tests/gen_big_script.sh 1000`. Use `ext_tests/test.sh big_script.txt` to run it and see the time of execution.