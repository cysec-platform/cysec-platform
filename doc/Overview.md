# The CYSEC questionnaire platform

## Basic structure

```
src/
   main/
       java/
       resources/
       webapp/
   test/
```

The folder `java` contains the main application code.
The folder `webapp` contains the jsp pages, static content and additional configuration files for both the web front-end and the main application.
`resources` contains templates, questionnaires, logs etc.

## Structure of the Java code

The entry point for the application is in the class ApplicationConfig, which sets up logging and registers a few classes that need to be available for dependency injection.
On top of that, it reads and stores certain path information according to the configuration in `context.xml`. Similarly, CacheBinder just sets up a detail for DI.

The central classes are Hooks, Questionnaires, Users and Companies. Each of these handles a subset of the API calls that the web app makes. Mobile should handle all the interaction with the mobile helper agent that isn't shared with the web front-end. 

Each of the classes is annotated with the path that they cover, as are all the methods. Public Methods with no Path annotation represent the "index" call.

### Hooks
This class represents the various events that occur while a questionnaire is being filled out by a user. Generally, these methods simply relay the call to the appropriate library for a given questionnaire. The methods here will typically return a list of commands to the front-end which works through them to update its state. The list of potential commands is defined in the `Commands` enum.
It has two helper methods, getQuestionByID (self-explanatory) and buildAnswer, which takes strings describing an answer and returns them as an object of type Answer.
onBegin is called when a questionnaire is started from the main page and the first question should be loaded.
onResume is used when another question is accessed through a direct link.
onResponseChange is called when a question is answered or the answer is changed.

### Questionnaires

This class is used to get rendered HTML for a questionnaire so that it can be displayed in the front-end as well as retrieving information about the questionnaires.

### The auth module

This module handles the login functionality. Session and SessionStore were preparatory work that so far has not been used.
AuthFilter is two-part: filter and doFilter allow it to both catch the requests going to the front-end and the API. It checks any login attempts against the users that have been loaded from XML. The Login class provides an endpoint that is used to pass auth requests from one part of the filter to the other.
CRYPT_TYPE, CryptPasswordStorage and PasswordStorage handle passwords.

## Running the software locally

Requirements:

* Java 8 (not 10, even with proper bytecode target!)
* Tomcat 9
* Maven

Ensure that all the dependencies have been installed through Maven and create the WAR file artifact. Deploy that onto Tomcat, with `/platform` as the context to give it the same context as the production environment.

Once Tomcat is running and has successfully deployed the artifact, `http://localhost:<port>/platform/app/` will bring you to the starting page of the app. `platform/api/hooks/` and `platform/api/rest/` are the locations of the API whch will be described later.
