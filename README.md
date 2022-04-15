# Rosey
Rosey is a generic and simple, yet powerful, interactive tool to process CSV files. It is named after the character **[Rosey](https://thejetsons.fandom.com/wiki/Rosey)** from The Jetsons cartoon.

![enter image description here](https://vignette.wikia.nocookie.net/thejetsons/images/1/15/Rosie_The_Robot_The_Jetsons_&_WWE_Robo-WrestleMania_%282%29.png/revision/latest/top-crop/width/300/height/300?cb=20200808224502)

Rosey provides a shell to load, process and keep track of data from CSV files using simple commands.

## Building 
To build the running artifact you'll need *Gradle 5* and *Java 11*.

    ./gradlew clean bootJar

## Running
After built the artifact you can run **Rosey** by just typing

    java -jar build/libs/rosey-<version>.jar

## Docker
Another way to run it is to use the docker image from *Dockerhub*

    docker run --rm --name rosey -it -v $HOME/.rosey:/usr/local/javaapps/rosey/run -p 9001:9001 jjocenio/rosey

## Using

This is a very easy interactive shell console. You can just type the commands you want. To start, you can try

    help

This will show you a list of available commands.

To have more details about a command, try

    help --comand "<complete-command>"

For example:  `help --comand "data load"`

You can use resource you may be used to like  `up`  and  `down`  to navigate on the history and  `tab`  to autocomplete commands.

To exit, type  `exit`,  `quit`  or press  `Ctrl+C`

### Loading data
Before to be able to actually process the data, you need to load it. Only common *CSV* file is supported for this.

    data load <path>

When data is loaded, you will see the count of rows in the prompt. You can also use `data count` or `data count-status` to check it. 

> **NOTE** if you are using docker, make sure to put the file in a shared volume.

If you want to check any record, you can just use `data get <row-id>`.

> You can set the `row id` in the *CSV* file in a column with one of  this names: `id` `row-id` `rowid`. Otherwise the row id will be generated automatically.

![data loading](https://raw.githubusercontent.com/jjocenio/rosey/main/assets/data-load.gif)

### Processing
You can process the data using the command `process <processor>`. You also need to specify if you want to process `all` rows or only `--row-id <row-id>`. 

When processing all records you can also limit the number of records with `--limit`.

Already processed rows won't be processed again, but you can force a retry for failed ones by using `--include-failed`.

**Rosey** supports 2 types of processors:

#### Text
This processor will apply a template transformation to the data. 

 - **`--output-template`**: specifies the Freemarker template to apply to the data. You can provide it inline, like `${row.data.columna}`; or you can provide a path to a template file using `@`, like `@/my/template/file.ext`.

#### HTTP
This processor will submit a *HTTP* request to a given *URL*. 

 - **`--url`**: specifies the target endpoint to send the request. You can use template syntax here, like `http://myserver.com/data/${row.id}`;
 - **`--http-method`**: defines the *HTTP* method to use (supported: `GET`, `POST`, `PUT` or `DELETE`. default: `GET`). 
 - **`--body`**: defines the body of the request (only for `POST` and `PUT` methods). You can use a template here, either inline or providing a file path prefixed with `@` like `@/my/template/file.ext`.
 - **`--headers`**: defines the headers for the request. Headers must be separated by `|` and must be in the format `Header-name: header value`. You can use a template here, either inline or providing a file path prefixed with `@` like `@/my/template/file.ext`.

### Output
All output is saved on internal database and you can check them individually by using `data get <row-id>`.

You can also process the output to a file or set of files using `--output-path` param. This param supports *Freemarker* template, thus you can use it to dynamically define the ouput file, like `/my/path/file-${row.id}`. 

If you want to put all output in one single file, you'll need to specify `--output-append`. If the file already exists, you need to use `--output-override`.

### Template data model
In every place you can use a template, you will have the following data model available:

 - **`row`**
	 - **`id`**: the row id
	 - **`status`**: the current status of the row
	 - **`resultDetail`**: if it failed, here will be the detail about the failure
	 - **`lastUpdate`**: the last time the row was updated
	 - **`data`**: here is the data from your *CSV* file
		 - **`column-name`**: for every column of your file, i.e. `row.data.first_name`
	 - **`output`**: the output of the processed row

> **NOTE** When using docker, always remember to put your files in a shared volume

![data loading](https://raw.githubusercontent.com/jjocenio/rosey/main/assets/process.gif)

### Accesing internal database
It's possible to access the internal HSQL database using any JDBC compatible client. Just point 
it to the following URL with username `sa` and with no password.

    jdbc:hsqldb:hsql://localhost:9001/rosey

## External references

 - **Freemarker**: https://freemarker.apache.org/docs/dgui_quickstart_template.html
