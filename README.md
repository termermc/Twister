# Twister
![Twister logo](https://termer.net/twister_small.png)

A fully featured webserver framework built on top of Twist

# What does Twister offer?
Twister is a highly versitile framework, with all the features of Twist (https://github.com/termermc/twist).

It include the following:
```
  - HTTPS support
  - Simple API built on top of SparkJava (http://sparkjava.com/)
  - Multi-site support
  - Multi-domain support
  - All the features in Twist
  - Support for GET, POST, DELETE, and PUT handlers
  - Support for pre-request handlers
  - Support for route handlers
  - Support for static document manipulation before response
  - Easy configurable redirects
  - File caching for lightning-quick responses
  - Reloading configurations without restarting the server
  - Timestamped logging
  - Shutting down the server via API
  - Portable module support with access to server API
  - Dependency loading
  - Simple database interaction with UniversalDB
  - Webpage rendering API
  - Module management via API
  - Easy configuration API using Maps
  - Jarfile loading API
  - and more
```

# How do I use it?
To use Twister, simply execute the jarfile in the terminal, and all the necessary files and directories will be generated.

Once the server has started, stop it and begin the **configuration** guide.

# Configuring the Webserver
In the directory Twister was started in, you will find the following files and directories:
```
modules/ - the directory to place modules
dependencies/ - where dependency jars for modules should be places
globalstatic/ (reconfigurable name) - where static files are served from
domains/ - the domain file system structure directory
404.html - the 404 page to be displayed
forbiddenpaths.ini - list of paths that are forbidden
linkeddomains.ini - list of domains that share the same filesystem and domain directory
twister.ini - the main configuration file
```

**modules/**
This is a directory that is used for placing modules in.

**dependencies/**
This is a directory where you place jars that modules depend on, so that they can be loaded by the server.

**globalstatic/**
The directory that houses all static files. Any files there can be access via get requests. If a user were to visit /image.png, the server would fetch globalstatic/image.png and serve it up. The location of "globalstatic" can be changed in **twister.ini**.

**domains/**
The core domain file system structure. It contains directories for domains, and in the domain directories are where static pages are to be placed. If you want to serve pages from the domain "localhost" you would create a directory called "localhost" inside of "domains/". Inside of that, you would place a file, such as "index.html" which would be served when a user visits "localhost". To have a webpage rendered on top of each page, create a file called "top.html" in the specific domain directory, and do the same but change it to "bottom.html" for bottom. To set a custom 404 page for the domain, simply create a "404.html" file in the domain root. Example of directory structure: ![example directory structure](https://termer.net/twist_structure.png)

**404.html**
The webpage to be rendered when a file or RequestHandler for a page is not found.

**forbiddenpaths.ini**
List of paths that are forbidden and cannot be accessed by users.

**linkeddomains.ini**
List of domains that share the same filesystem and domain directory

**twister.ini**
The main configuration file. Descriptions for settings can be found in the file itself.

# Configuring redirects
```
Domain redirect files are useful for link shortening, as well as fixing links that are in a new location.
  To add a domain redirect file, simply create a file called "redirects.ini" inside the root of your desired
  domain directory.
  To add a redirect for a specific path, simply add the following line (replacing the uppercase text):
    PATH > REDIRECT_URL
  To make all requests to the desired domain redirect, add the following line (replacing the uppercase text):
    * > REDIRECT_URL
  Example:
    /redirect/ > http://www.example.com/
 ```
 
# How do I build it?
To build, simply run `mvn install` in the project directory. You will get a file called `Twister-x.x-jar-with-dependencies.jar` in the `target` directory.

=======
To build Twister, you must include [Zip4J](http://www.lingala.net/zip4j/), [SparkJava](https://github.com/perwendel/spark) and [UniversalDB](https://github.com/TermerMC/UniversalDB) in the classpath, and then build as a runnable jar.

NOTE: It is recommended that you use maven to include Spark and Zip4J, as it will automatically download dependencies.

# How do I create a module?
An example module can be found at https://github.com/termermc/ExampleTwisterModule with instructions on how to build and API examples.

# Where can I find the Javadoc?
Here: https://termer.net/javadocs/twister/

Click the version you want, and you will be presented with the Javadoc.

# Current development version
The current version being developed is version **1.1**. In 1.1, embedded Java scripting inside of HTML documents
if being added.

Checklist:
  - **DONE** Embedded Java scripting inside of HTML documents
=======
There is currently no indev version.