# The Quest for the Holy Grails: Grails 8 Labs

These labs build a complete Apache Grails 8 application, one stage at a time, around a Monty Python quest domain: knights, castles, quests, tasks, and the enemies that stand in the way. Each lab ends with a git tag, so you can skip ahead, catch up, or compare your work against the finished stage at any point.

The labs accompany the talk *The Quest for the Holy Grails: A Grails Tutorial* given at Community Over Code, Glasgow, 12 October 2026. Grails 8.0.0 was released the day before, on 11 October. The labs were written on Grails 7.2.4 and moved to 8.0.0 that day, so Lab 9 covers what changed in both releases. The talk walks through a subset of these labs live; the rest are here for you to do on your own.

## Prerequisites

- **JDK 25** (Grails 8 needs at least Java 21, and 21 works just as well)
- **Grails 8.0.0** (released 11 October 2026)
- **Gradle 9.8.0** (provided by the wrapper, nothing to install)
- An IDE with Groovy support. IntelliJ IDEA Ultimate has a Grails plugin; Community Edition works as a plain Gradle project.
- **Optional:** a container runtime such as Docker Desktop, OrbStack, or Colima, for the Testcontainers browser test in the final lab

The easiest way to get matching tool versions is [SDKMAN](https://sdkman.io):

```bash
sdk install java 25.0.4-tem
sdk install grails 8.0.0
```

The project contains an `.sdkmanrc` file that pins both versions. Once you have them installed, running `sdk env` inside the project directory selects them for your current shell.

> **Note:** Grails 8 builds with Gradle 9.8, which supports Java 25, so IntelliJ IDEA syncs the project on JDK 25 without complaint. That was not true of Grails 7: it was pinned to Gradle 8.14, which supports Java 24 at most, and IntelliJ refused to open a 7.2.4 project on JDK 25. Java 21 is the floor; the generated build sets `compileJava.options.release = 21`.

> [!IMPORTANT]
> SDKMAN adds newly installed tools to your `PATH` when a shell starts. If you install Grails and then get `grails: command not found` in a terminal you already had open, either open a new terminal or run `source ~/.sdkman/bin/sdkman-init.sh`.

### Using the git tags

The repository is tagged at the end of every lab. To see where a lab finishes, or to start a lab from a known-good state, check out its tag:

```bash
git checkout step1-quest
```

To return to the latest state, check out `main`. Tag names appear at the end of each lab under **Checkpoint**.

The tags were cut while these labs were on Grails 7.2.4, and they still build on it: each tag carries the 7.2.4 build files and a Gradle 8.14 wrapper, which runs on JDK 21, not 25. Each tag's `.sdkmanrc` names that JDK, so run `sdk env` after checking out a tag, and `sdk env` again on `main`. The application code at every stage is identical on Grails 8, so a tag remains the right thing to diff your work against; `main` is the Grails 8 build.

## Table of Contents

0. [Creating the Project](#lab-0-creating-the-project)
1. [The First Domain Class](#lab-1-the-first-domain-class)
2. [Adding a Related Domain Class](#lab-2-adding-a-related-domain-class)
3. [Unit Testing Domain Classes](#lab-3-unit-testing-domain-classes)
4. [Dynamic Finders, Criteria, and Where Queries](#lab-4-dynamic-finders-criteria-and-where-queries)
5. [Completing the Domain Model](#lab-5-completing-the-domain-model)
6. [Scaffolding with @Scaffold](#lab-6-scaffolding-with-scaffold)
7. [A Geocoder Service](#lab-7-a-geocoder-service)
8. [Mapping the Castles](#lab-8-mapping-the-castles)
9. [What's New in Grails 8 (and 7)](#lab-9-whats-new-in-grails-8-and-7)
10. [Enemies: Inheritance in GORM](#lab-10-enemies-inheritance-in-gorm)
11. [Optional: A JSON API for the Quest](#lab-11-optional-a-json-api-for-the-quest)

## Lab 0: Creating the Project

Grails applications start life in **Grails Forge**, the web-based project generator at https://grails.apache.org/start. Forge replaces the older `grails create-app` wizard as the recommended starting point, and it also has a plain HTTP API, which is what we use here so that the result is reproducible.

### Step 1: Generate the application

1. Create a directory for the project and change into it:

   ```bash
   mkdir holygrails8
   cd holygrails8
   ```

2. Download the generated application from the Forge API. The URL names the application type (`web`), the fully qualified application name, and the options we want:

   ```bash
   curl -L -o app.zip "https://latest.grails.org/create/web/com.kousenit.holygrails?jdkVersion=JDK_25&gorm=HIBERNATE&servlet=TOMCAT&test=SPOCK&features=testcontainers"
   ```

   The pieces of that URL:

   | Part | Meaning |
   |---|---|
   | `web` | A full web application with GSP views, as opposed to `rest-api` |
   | `com.kousenit.holygrails` | Package `com.kousenit`, application name `holygrails` |
   | `gorm=HIBERNATE` | GORM over Hibernate with an H2 in-memory database |
   | `test=SPOCK` | Spock for unit and integration tests |
   | `features=testcontainers` | Adds Testcontainers for the containerized Geb browser tests in Lab 9 |

   If you prefer the web page, choose the same options there, click **Generate Project**, and download the zip.

3. Unzip the archive into the current directory. The zip contains a single top-level folder named after the application, so flatten it:

   ```bash
   unzip -q app.zip
   mv holygrails/* holygrails/.[!.]* .
   rmdir holygrails
   rm app.zip
   ```

4. Look at what you have:

   ```
   build.gradle           Gradle build using the Grails BOM and Gradle plugins
   gradle.properties      grailsVersion=8.0.0
   grails-app/            The Grails application: conf, controllers, domain, services, views, ...
   src/main/groovy        Plain Groovy sources
   src/test/groovy        Unit tests
   src/integration-test   Integration and functional tests
   grailsw, gradlew       Wrapper scripts; no global install needed
   ```

> **Note:** Every Grails artifact since 7 lives under the `org.apache.grails` group. If you have an older Grails project, you will see `org.grails` coordinates instead. The `build.gradle` here uses `platform("org.apache.grails:grails-bom:$grailsVersion")` so that individual artifact versions are never spelled out.

### Step 2: Pin the tool versions

1. Create a file called `.sdkmanrc` in the project root:

   ```
   java=25.0.4-tem
   grails=8.0.0
   ```

2. Select those versions for your shell:

   ```bash
   sdk env
   ```

   SDKMAN prints the versions it switched to. From now on, run `sdk env` whenever you open a new terminal in this project.

### Step 3: Build and test

1. Run the (currently empty) test suite to download dependencies and prove the build works:

   ```bash
   ./gradlew test
   ```

   The first run downloads the Gradle distribution and the Grails dependencies, so give it a minute. It should finish with `BUILD SUCCESSFUL`.

> **Note:** If you have `org.gradle.configuration-cache=true` in `~/.gradle/gradle.properties`, the Grails 8 build honours it; the `buildProperties` task that broke under the configuration cache in Grails 7.2.4 was fixed for 8.

### Step 4: Run the application

1. Start the app:

   ```bash
   grails run-app
   ```

   or, equivalently, `./gradlew bootRun`. Both start an embedded Tomcat on port 8080.

> **Note:** The project also contains `grailsw`, a wrapper script in the style of `gradlew`. It reads `grailsVersion` from `gradle.properties`, downloads that Grails distribution into `~/.grails/wrapper` on first use, and runs it, so `./grailsw run-app` works on a machine with only a JDK installed. Inside a project the two commands are equivalent; these labs use the SDKMAN-installed `grails` throughout.

2. Watch the console. Grails prints a banner with the versions it is running on:

   ```
          holygrails8: 0.1 | JVM: Eclipse Adoptium 25.0.4 | Grails: 8.0.0
       Groovy: 5.1.3 | Spring Boot: 4.1.1 | Spring: 7.0.9 | Tomcat: 11.0.24.0
   Grails application running at http://localhost:8080 in environment: development
   ```

   The banner arrived in Grails 7, the dependency versions in it in 7.1, and Grails 8 colours it and adds the servlet container. It is the quickest way to answer "what is this app actually running on?"

3. Open http://localhost:8080 in a browser. The welcome page lists the installed plugins, the controllers (none yet), and the artefacts in the application.

4. Stop the application with Ctrl-C.

### Step 5: Import into IntelliJ IDEA

1. From the project root, run `idea .` if you have the JetBrains Toolbox command-line launcher, or use **File → Open...** and choose the project directory.
2. IntelliJ recognizes `build.gradle` and imports the project. Accept the defaults.
3. Check **Settings → Build, Execution, Deployment → Build Tools → Gradle** and make sure **Gradle JVM** is the JDK you selected with SDKMAN. IntelliJ does not read `.sdkmanrc`.
4. With IntelliJ Ultimate, the Grails plugin recognizes the `grails-app` layout and adds a **Grails** tool window. Community Edition shows a normal Gradle project, which is all these labs require.

### Step 6: Put it under version control

1. Forge generates a `.gitignore` that covers `build/`, `.gradle/`, and IDE files. Initialize the repository and commit the untouched starter:

   ```bash
   git init -b main
   git add -A
   git commit -m "Grails 8.0.0 starter app from Forge"
   git tag step0-starter
   ```

   Committing the generated project before touching it means every later diff shows exactly what *you* added.

### Key Learning Points

- Grails Forge at https://grails.apache.org/start generates projects, and its HTTP API makes the generation reproducible from a single `curl` command.
- Grails artifacts use the `org.apache.grails` group and are versioned through a BOM, so `build.gradle` names no individual versions.
- `grails run-app` and `./gradlew bootRun` are the same thing: an embedded Tomcat running a Spring Boot application.
- The startup banner reports the JVM, Grails, Groovy, Spring Boot, Spring, and Tomcat versions.
- SDKMAN's `.sdkmanrc` and `sdk env` keep tool versions consistent across machines.

### Checkpoint

```bash
git checkout step0-starter
```

## Lab 1: The First Domain Class

Everything in a Grails application radiates out from its **domain classes**. A domain class is a Groovy class in `grails-app/domain` that GORM, the Grails object-relational mapper, turns into a database table, a set of persistence methods, and a validation contract. In this lab you create the `Quest` class, give it a constraint, generate a web interface for it, and fix the tests that Grails generates alongside.

### Step 1: Create the domain class

1. From the project root, create the class with the Grails CLI:

   ```bash
   grails create-domain-class Quest
   ```

   The command creates two files and reports them:

   ```
   | Created grails-app/domain/com/kousenit/Quest.groovy
   | Created src/test/groovy/com/kousenit/QuestSpec.groovy
   ```

   The package comes from `grails.codegen.defaultPackage` in `grails-app/conf/application.yml`, which Forge set to `com.kousenit`.

2. Open `grails-app/domain/com/kousenit/Quest.groovy`. The generated class is nearly empty:

   ```groovy
   package com.kousenit

   class Quest {

       static constraints = {
       }
   }
   ```

### Step 2: Add a property and a constraint

1. Give the quest a name, a `toString` so it reads well in the scaffolded pages, and a constraint that forbids blank names:

   ```groovy
   package com.kousenit

   class Quest {
       String name

       String toString() { name }

       static constraints = {
           name blank: false
       }
   }
   ```

   The `constraints` block is a Groovy DSL. Each line names a property and applies one or more constraints to it. `blank: false` rejects the empty string and whitespace-only strings. Every property is also **non-nullable by default** in GORM, so you never have to say `nullable: false`.

> **Note:** GORM supplies `id` and `version` properties automatically. You will see both as columns in the `QUEST` table and the `version` column drives optimistic locking on updates.

### Step 3: Generate the scaffolding

Grails can generate a complete create, read, update, delete interface for a domain class. Grails offers two styles since 7: `generate-all` writes real source files you can read and edit, and the `@Scaffold` annotation (Lab 6) generates everything at runtime. We start with the files so you can see what a Grails controller looks like.

1. Run the generator with the fully qualified class name:

   ```bash
   grails generate-all com.kousenit.Quest
   ```

2. Look at what appeared:

   | File | Purpose |
   |---|---|
   | `grails-app/controllers/com/kousenit/QuestController.groovy` | Seven actions: index, show, create, save, edit, update, delete |
   | `grails-app/services/com/kousenit/QuestService.groovy` | A GORM **data service**: an interface GORM implements for you |
   | `grails-app/views/quest/{index,show,create,edit}.gsp` | Groovy Server Pages for each screen |
   | `src/test/groovy/com/kousenit/QuestControllerSpec.groovy` | Unit test for the controller |
   | `src/integration-test/groovy/com/kousenit/QuestServiceSpec.groovy` | Integration test for the service |

3. Read `QuestService.groovy`. It is only an interface:

   ```groovy
   @Service(Quest)
   interface QuestService {
       Quest get(Serializable id)
       List<Quest> list(Map args)
       Long count()
       void delete(Serializable id)
       Quest save(Quest quest)
   }
   ```

   GORM reads the method names and generates the implementation at compile time. Each method is transactional. The controller injects it by property name, `QuestService questService`, and calls it rather than touching the domain class directly.

4. Skim `QuestController.groovy`. Notice the pattern in `save`: bind the request to a `Quest`, call the service, and on a `ValidationException` re-render the `create` view with the errors. The `request.withFormat` block means the same action answers an HTML form with a redirect and a JSON client with a `201 Created`.

### Step 4: Run it and break it

1. Start the application:

   ```bash
   grails run-app
   ```

2. Open http://localhost:8080. The welcome page now lists `QuestController` under **Available Controllers**. Click it.

3. Click **New Quest**, enter `Seek the grail`, and save. You get a show page for quest 1, and the list page shows your quest with its `toString` value.

4. Now click **New Quest** again and type a few spaces into the name. The browser's HTML5 `required` attribute may stop you first, since the scaffolding adds it for non-nullable properties. If so, submit a quest with a single space instead of nothing. The error that comes back is:

   ```
   Property [name] of class [class com.kousenit.Quest] cannot be null
   ```

   That is the *nullable* error, not the *blank* one. Grails data binding trims strings and, by default, converts empty strings to `null` before validation runs, so `blank: false` never gets a look.

5. Stop the app with Ctrl-C.

### Step 5: Make the blank constraint fire, with a better message

1. Turn off the empty-string conversion in `grails-app/conf/application.yml`, inside the existing `grails:` block:

   ```yaml
   grails:
       codegen:
           defaultPackage: com.kousenit
       databinding:
           convertEmptyStringsToNull: false
   ```

   Trimming still happens, so a name of three spaces becomes the empty string, which `blank: false` rejects.

2. Replace the default error text. Grails looks up validation messages by the key `className.propertyName.constraint`. Add this line to the end of `grails-app/i18n/messages.properties`:

   ```properties
   quest.name.blank=Quests must have a name
   ```

3. Run the app again and submit a quest with a blank name. Now you see **Quests must have a name**.

### Step 6: Fix the generated tests

`generate-all` writes test skeletons that fail on purpose, each with a TODO and an `assert false`, so you cannot forget them. Run the unit tests and watch three fail:

```bash
./gradlew test
```

1. Replace the body of `src/test/groovy/com/kousenit/QuestSpec.groovy` with two real tests:

   ```groovy
   package com.kousenit

   import grails.testing.gorm.DomainUnitTest
   import spock.lang.Specification

   class QuestSpec extends Specification implements DomainUnitTest<Quest> {

       void "a quest with a name is valid"() {
           expect:
           new Quest(name: 'Seek the grail').validate()
       }

       void "a quest needs a name"() {
           when:
           Quest quest = new Quest(name: ' ')

           then:
           !quest.validate()
           quest.errors['name'].code == 'blank'
       }
   }
   ```

   `DomainUnitTest` gives the class its GORM methods without a database. The second test depends on the `convertEmptyStringsToNull` setting from Step 5; without it, the error code would be `nullable`.

2. In `src/test/groovy/com/kousenit/QuestControllerSpec.groovy`, replace the `populateValidParams` method:

   ```groovy
   def populateValidParams(params) {
       assert params != null
       params["name"] = 'Seek the grail'
   }
   ```

   Everything else in that spec is already correct. It mocks `QuestService` with Spock's `Mock()` and checks redirects and flash messages for every action.

3. In `src/integration-test/groovy/com/kousenit/QuestServiceSpec.groovy`, replace `setupData` and the `save` test. The other tests expect five quests and need the id of one of them:

   ```groovy
   private Long setupData() {
       new Quest(name: 'Seek the grail').save(flush: true, failOnError: true)
       new Quest(name: 'Find a shrubbery').save(flush: true, failOnError: true)
       Quest quest = new Quest(name: 'Cross the Bridge of Death').save(flush: true, failOnError: true)
       new Quest(name: 'Storm the Castle Anthrax').save(flush: true, failOnError: true)
       new Quest(name: 'Consult Tim the Enchanter').save(flush: true, failOnError: true)
       quest.id
   }
   ```

   ```groovy
   void "test save"() {
       when:
       Quest quest = new Quest(name: 'Seek the grail')
       questService.save(quest)

       then:
       quest.id != null
   }
   ```

   Also change the `get` test to use the returned id rather than a hard-coded `1`, and delete the two remaining `assert false` lines. The spec is annotated `@Integration`, which boots the whole application, and `@Rollback`, which undoes each test's database changes.

4. Run both test phases:

   ```bash
   ./gradlew test
   ./gradlew integrationTest
   ```

> [!IMPORTANT]
> The integration phase includes `HolygrailsSpec`, a functional test Forge generated that extends `ContainerGebSpec`. It drives a real browser running in a Docker container, so **Docker Desktop (or OrbStack, Colima, Podman, Rancher) must be running**, and the first run downloads a browser image. If you want to skip it for now, run only the service spec:
>
> ```bash
> ./gradlew integrationTest --tests 'com.kousenit.QuestServiceSpec'
> ```

> [!WARNING]
> **On Apple silicon the Geb test fails out of the box.** The default image, `selenium/standalone-chrome`, is published for amd64 only. Docker runs it under emulation and Chrome dies the moment the test connects, with `NoSuchSessionException: session deleted as the browser has closed the connection`. Firefox's image is multi-arch, so switch to it. Create `src/integration-test/resources/GebConfig.groovy`:
>
> ```groovy
> import org.openqa.selenium.firefox.FirefoxOptions
> import org.openqa.selenium.remote.RemoteWebDriver
>
> driver = { new RemoteWebDriver(new FirefoxOptions()) }
> containerBrowser = 'firefox'
> ```
>
> and add the Firefox driver next to the Geb line in `build.gradle`:
>
> ```groovy
> integrationTestImplementation testFixtures("org.apache.grails:grails-geb")
> integrationTestImplementation "org.seleniumhq.selenium:selenium-firefox-driver"
> ```
>
> The plugin builds the image name as `selenium/standalone-<containerBrowser>`. Chromium also has an arm64 image, but Testcontainers rejects it as an unknown substitute for the Chrome image, so Firefox is the one-line answer.
>
> **The alternative is a Docker setting.** The crash happens when Docker Desktop's newer *Docker VMM* backend emulates amd64 with QEMU. In **Settings → General**, choose **Apple Virtualization framework** as the virtual machine option and tick **Use Rosetta for x86_64/amd64 emulation**. After Docker restarts, the default Chrome image works unchanged, and you can delete `GebConfig.groovy`. The repository keeps the Firefox configuration because it works on every Docker setup without touching settings.

### Key Learning Points

- A domain class is a plain Groovy class in `grails-app/domain`. GORM adds `id`, `version`, persistence methods, and validation.
- Constraints are a DSL. Properties are non-nullable unless you say otherwise.
- `generate-all` produces a controller, a GORM data service interface, four GSP views, and two test skeletons. The skeletons fail until you fill them in.
- Data binding trims strings and converts empty strings to `null` by default. Set `grails.databinding.convertEmptyStringsToNull: false` when you want `blank: false` to do its job.
- Validation messages follow the key pattern `className.propertyName.constraint` in `messages.properties`.
- `@Integration` tests boot the application; `@Rollback` keeps them from leaving data behind.
- `ContainerGebSpec` runs functional tests in a containerized browser. On Apple silicon, point `GebConfig.groovy` at Firefox.

### Checkpoint

```bash
git checkout step1-quest
```

## Lab 2: Adding a Related Domain Class

A quest is a list of tasks. In this lab you add a `Task` class, make it belong to a `Quest`, and use three kinds of constraint: a range, a cross-property validator, and the defaults GORM applies on its own. Along the way you meet `java.time` in domain classes, derived properties, and automatic timestamps.

### Step 1: Create the Task class

1. Generate the class and its test skeleton:

   ```bash
   grails create-domain-class Task
   ```

2. Replace the contents of `grails-app/domain/com/kousenit/Task.groovy`:

   ```groovy
   package com.kousenit

   import groovy.transform.ToString

   import java.time.LocalDate

   @ToString(includeNames = true, includes = ['name', 'priority', 'completed'])
   class Task {
       String name
       int priority = 3
       LocalDate startDate = LocalDate.now()
       LocalDate endDate = LocalDate.now()
       boolean completed

       static belongsTo = [quest: Quest]

       int getDuration() { (endDate - startDate) + 1 }

       static constraints = {
           name blank: false
           priority range: 1..5
           endDate validator: { LocalDate value, Task task ->
               value >= task.startDate
           }
       }
   }
   ```

   Points to notice:

   - **`belongsTo`** makes `Quest` the owning side. Deleting a quest cascades to its tasks, and `quest` becomes a required property on `Task`.
   - **`range: 1..5`** uses a Groovy range as a constraint. Values outside it fail with the codes `range.toosmall` or `range.toobig`.
   - **`validator:`** takes a closure. With two parameters it receives the property value and the whole instance, so it can compare `endDate` to `startDate`. Returning `false` produces the error code `validator.invalid`.
   - **`getDuration()`** is a getter with no backing field. GORM treats getter-only properties as non-persistent, so there is no `duration` column and no `static transients` list is needed, unlike older Grails versions.
   - **`LocalDate`** maps to a `DATE` column. The subtraction `endDate - startDate` is Groovy's operator overloading for dates, which returns the number of days between them.

> [!IMPORTANT]
> That subtraction will not compile as the project stands. Groovy is modular (since 4, and 5 is the same), and the date operators live in the `groovy-datetime` module. Grails includes `groovy-json`, `groovy-sql`, `groovy-templates`, and `groovy-xml`, but not `groovy-datetime`. Add it to the `dependencies` block of `build.gradle`, right after `grails-core`. The Groovy BOM supplies the version:
>
> ```groovy
> implementation "org.apache.groovy:groovy-datetime"
> ```
>
> You get `date - date`, `date + 1`, `date.format('MMM d')`, and ranges over dates in return.

   - **`@ToString`** from Groovy generates `toString()` so you do not have to write one.

### Step 2: Complete the other side of the relationship

1. Edit `grails-app/domain/com/kousenit/Quest.groovy` to declare the collection of tasks and two timestamp properties:

   ```groovy
   package com.kousenit

   import java.time.LocalDateTime

   class Quest {
       String name
       LocalDateTime dateCreated
       LocalDateTime lastUpdated

       static hasMany = [tasks: Task]

       String toString() { name }

       static constraints = {
           name blank: false
       }
   }
   ```

   `hasMany` gives `Quest` a `Set<Task> tasks` plus an `addToTasks` method. The property names `dateCreated` and `lastUpdated` are special: GORM fills them in automatically on insert and update. They work with `java.time` types as well as `java.util.Date`.

### Step 3: Generate the scaffolding

```bash
grails generate-all com.kousenit.Task
```

This produces the same set of files as for `Quest`: controller, data service, four views, and two test skeletons. Open `grails-app/views/task/create.gsp` and notice that the whole form is one tag, `<f:all bean="task" .../>` from the Fields plugin, which renders a widget for each property: a select for `quest` populated from the database, day, month, and year selects for the dates, and a checkbox for `completed`.

### Step 4: Try it

1. Start the app with `grails run-app` and create a quest, then go to **TaskController → New Task**. The **Quest** dropdown lists your quest by its `toString` value.

2. Create a task with an end date earlier than its start date. The default error reads:

   ```
   Property [endDate] of class [class com.kousenit.Task] with value [...] does not pass custom validation
   ```

3. Replace that with a message. The key for a custom validator is `className.propertyName.validator.invalid`. Add to `grails-app/i18n/messages.properties`:

   ```properties
   task.endDate.validator.invalid=A task cannot end before it starts
   ```

   Restart and try again. Then try a priority of 9 and read the range error, which already includes the bounds.

4. Open the quest's show page. The scaffolding renders the `tasks` collection as links. The `dateCreated` and `lastUpdated` values are stored but not shown; the scaffolding templates leave them out on purpose.

### Step 5: Fill in the tests

1. Replace `src/test/groovy/com/kousenit/TaskSpec.groovy`:

   ```groovy
   package com.kousenit

   import grails.testing.gorm.DomainUnitTest
   import spock.lang.Specification

   class TaskSpec extends Specification implements DomainUnitTest<Task> {

       Quest quest = new Quest(name: 'Seek the grail')
       Task task = new Task(name: 'Defeat the Black Knight', quest: quest)

       void "a task with defaults is valid"() {
           expect:
           task.validate()
           task.priority == 3
           !task.completed
       }

       void "a task that starts and ends today lasts one day"() {
           expect:
           task.duration == 1
       }

       void "the end date may not precede the start date"() {
           when:
           task.endDate = task.startDate.minusDays(1)

           then:
           !task.validate()
           task.errors['endDate'].code == 'validator.invalid'
       }
   }
   ```

   Spock creates a fresh `TaskSpec` instance for every feature method, so the `task` field is a new object in each test.

2. In `TaskControllerSpec.groovy`, provide the valid parameters:

   ```groovy
   def populateValidParams(params) {
       assert params != null
       params["name"] = 'Defeat the Black Knight'
       params["priority"] = 2
   }
   ```

3. In `src/integration-test/groovy/com/kousenit/TaskServiceSpec.groovy`, every task needs a quest, so `setupData` creates one first:

   ```groovy
   private Long setupData() {
       Quest quest = new Quest(name: 'Seek the grail').save(flush: true, failOnError: true)
       new Task(name: 'Run away from killer rabbit', quest: quest).save(flush: true, failOnError: true)
       new Task(name: 'Answer the Bridgekeeper', priority: 4, quest: quest).save(flush: true, failOnError: true)
       Task task = new Task(name: 'Defeat the Black Knight', completed: true, quest: quest).save(flush: true, failOnError: true)
       new Task(name: 'Bring out your dead', quest: quest).save(flush: true, failOnError: true)
       new Task(name: 'Find a shrubbery', priority: 2, quest: quest).save(flush: true, failOnError: true)
       task.id
   }
   ```

   and the `save` test does the same:

   ```groovy
   void "test save"() {
       when:
       Quest quest = new Quest(name: 'Seek the grail').save(flush: true, failOnError: true)
       Task task = new Task(name: 'Defeat the Black Knight', quest: quest)
       taskService.save(task)

       then:
       task.id != null
   }
   ```

   As in Lab 1, change the `get` test to use the returned id and remove the leftover `assert false` lines.

4. Run everything:

   ```bash
   ./gradlew test
   ./gradlew integrationTest
   ```

### Key Learning Points

- `belongsTo` on the child and `hasMany` on the parent make a one-to-many with cascading deletes and an `addTo...` method.
- Constraints include ranges and custom validator closures; the validator's second parameter is the whole instance.
- Getter-only properties are derived, not persisted. Grails needs no `transients` declaration for them.
- `dateCreated` and `lastUpdated` are filled in by GORM and work with `java.time`.
- Groovy 5 is modular, as 4 was. Grails ships `groovy-json`, `groovy-sql`, `groovy-templates`, and `groovy-xml`; add `groovy-datetime` yourself for date operators.
- Custom validator messages use the key `className.propertyName.validator.invalid`.

### Checkpoint

```bash
git checkout step2-task
```

## Lab 3: Unit Testing Domain Classes

Grails tests are written in [Spock](https://spockframework.org), and the `grails-testing-support` libraries supply traits that stand up just enough of Grails for the class under test. In this lab you test every constraint on `Task` without a database, and you learn the error codes each constraint produces, which is what your `messages.properties` keys are built from.

### Step 1: Know the traits

Open `src/test/groovy/com/kousenit/TaskSpec.groovy`. It implements `DomainUnitTest<Task>`. That trait gives `Task` its GORM methods (`validate()`, `save()`, `count()`, dynamic finders) backed by an in-memory simple datastore, with no Hibernate and no H2. It also registers constraints, so `validate()` and `errors` behave as they do in the running application.

Other traits you will meet: `ControllerUnitTest<T>` (used by the generated controller specs), `ServiceUnitTest<T>` (Lab 7), and `DataTest`, which lets a unit test mock several domain classes at once with `mockDomains(Quest, Task)`.

### Step 2: Test every constraint by its error code

Replace `TaskSpec.groovy` with the version below. Each test changes one property on the shared `task` and checks both that validation fails and *which* constraint failed:

```groovy
package com.kousenit

import grails.testing.gorm.DomainUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class TaskSpec extends Specification implements DomainUnitTest<Task> {

    @Shared Quest quest = new Quest(name: 'Seek the grail')
    Task task = new Task(name: 'Defeat the Black Knight', quest: quest)

    void "a task with defaults is valid"() {
        expect:
        task.validate()
        task.priority == 3
        !task.completed
    }

    void "a task that starts and ends today lasts one day"() {
        expect:
        task.duration == 1
    }

    void "duration counts both end points"() {
        when: 'the task ends two days after it starts'
        task.endDate = task.startDate.plusDays(2)

        then: 'it lasts three days'
        task.duration == 3
    }

    void "a blank name is not valid"() {
        when:
        task.name = ' '

        then:
        !task.validate()
        task.errors['name'].code == 'blank'
    }

    void "a task needs a quest"() {
        when:
        task.quest = null

        then:
        !task.validate()
        task.errors['quest'].code == 'nullable'
    }

    void "priorities below 1 are not valid"() {
        when:
        task.priority = 0

        then:
        !task.validate()
        task.errors['priority'].code == 'range.toosmall'
    }

    void "priorities above 5 are not valid"() {
        when:
        task.priority = 6

        then:
        !task.validate()
        task.errors['priority'].code == 'range.toobig'
    }

    @Unroll
    void "a task with priority #priority is valid"() {
        when:
        task.priority = priority

        then:
        task.validate()

        where:
        priority << (1..5)
    }

    void "the end date may not precede the start date"() {
        when:
        task.endDate = task.startDate.minusDays(1)

        then:
        !task.validate()
        task.errors['endDate'].code == 'validator.invalid'
    }
}
```

Things to notice:

- **`@Shared`** keeps one `Quest` for all tests, since none of them change it. The `task` field has no annotation, so Spock builds a fresh one for every feature method; changing its priority in one test cannot leak into the next.
- **`task.errors['name'].code`** reads the Spring `Errors` object GORM attaches after validation. The code is the last segment of the message key: `quest.name.blank` in `messages.properties` corresponds to code `blank` on property `name` of class `Quest`.
- **`@Unroll`** with a `where:` block turns one feature method into five, one per priority. The `#priority` in the method name is replaced for each, so the test report lists *a task with priority 1 is valid* through *a task with priority 5 is valid*.
- The `when:` and `then:` labels take optional descriptions. Use them when the test name alone does not say what the setup means.

### Step 3: Run the tests

```bash
./gradlew test
```

Open `build/reports/tests/test/index.html` to see the unrolled names. Try breaking something, such as changing the range to `1..4`, to watch which tests catch it.

### Key Learning Points

- `DomainUnitTest<T>` tests a domain class with no database. Validation and GORM methods work; Hibernate is not involved.
- Test constraints by their error code: `blank`, `nullable`, `range.toosmall`, `range.toobig`, `validator.invalid`. Those codes are what you customize in `messages.properties`.
- `@Shared` for fixtures that no test mutates; plain fields for per-test state.
- `@Unroll` plus a `where:` block replaces copy-and-paste tests.

### Checkpoint

```bash
git checkout step3-testing
```

## Lab 4: Dynamic Finders, Criteria, and Where Queries

GORM gives you four ways to ask a question of the database, from the terse to the composable: dynamic finders, criteria, where queries, and HQL. Earlier versions of this course explored them in the interactive Grails console (`grails console`, still available: in Grails 8 the console and the other commands come from companion `-cli` artifacts that the Gradle plugin adds for you, so `build.gradle` no longer mentions it). An integration test is a better vehicle for a tutorial, because each query sits next to the answer you expect from it, and it keeps running after the lab is over. First, though, the application needs some data.

### Step 1: See the SQL

GORM hides SQL from you, which is pleasant until you want to know what a finder actually does. Turn on SQL logging for the development environment only, in `grails-app/conf/application.yml` under `environments: development: dataSource:`:

```yaml
environments:
  development:
    dataSource:
      dbCreate: create-drop
      logSql: true
      formatSql: true
      url: jdbc:h2:mem:devDb;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE
```

Keep it out of `test`, where it turns the build log into wallpaper.

### Step 2: Seed data on startup

Grails runs `grails-app/init/com/kousenit/BootStrap.groovy` once at startup. We want the same data in the running app and in the query tests, so put it in a plain class that both can call.

1. Create `src/main/groovy/com/kousenit/SeedData.groovy`:

   ```groovy
   package com.kousenit

   import java.time.LocalDate

   class SeedData {

       static Quest seekTheGrail() {
           LocalDate today = LocalDate.now()
           new Quest(name: 'Seek the grail')
                   .addToTasks(name: 'Run away from killer rabbit', priority: 1)
                   .addToTasks(name: 'Answer the Bridgekeeper', priority: 4, startDate: today + 1, endDate: today + 1)
                   .addToTasks(name: 'Defeat the Black Knight', completed: true, startDate: today - 3, endDate: today - 2)
                   .addToTasks(name: 'Bring out your dead', completed: true, priority: 5, startDate: today - 1, endDate: today - 1)
                   .addToTasks(name: 'Find a shrubbery for the Knights Who Say Ni', priority: 2, endDate: today + 7)
                   .addToTasks(name: 'Get taunted by the French', completed: true, priority: 4)
                   .addToTasks(name: 'Weigh a witch against a duck', priority: 3)
                   .addToTasks(name: 'Build a giant wooden rabbit', priority: 2, endDate: today + 14)
                   .addToTasks(name: 'Lobbest thou thy Holy Hand Grenade of Antioch', priority: 5, startDate: today + 2, endDate: today + 2)
                   .save(failOnError: true)
       }
   }
   ```

   `addToTasks` accepts a map, builds the `Task`, sets its `quest`, and returns the quest, so the calls chain. Saving the quest cascades to the nine tasks. The dates use the Groovy operators from Lab 2.

   `failOnError: true` matters. By default `save()` returns `null` on a validation failure and carries on; in startup code you want it to throw.

2. Replace `BootStrap.groovy`:

   ```groovy
   package com.kousenit

   import grails.util.Environment

   class BootStrap {

       def init = { servletContext ->
           if (Environment.current != Environment.TEST && Quest.count() == 0) {
               SeedData.seekTheGrail()
           }
       }

       def destroy = {
       }
   }
   ```

   The guard keeps seed data out of the test environment, where tests set up their own, and out of a database that already has quests in it.

3. Start the app and open http://localhost:8080/quest/show/1. The quest lists its nine tasks, and the console shows the formatted `insert` statements that put them there.

4. The task links read like `com.kousenit.Task(name:Bring out your dead, priority:5, completed:false)`. That is the `@ToString` output from Lab 2, which suited the test reports but not a web page. Replace the annotation with a plain method:

   ```groovy
   String toString() { name }
   ```

   and delete the `import groovy.transform.ToString`. Restart and the links show task names.

### Step 3: Create the integration test

```bash
grails create-integration-test QuestQueries
```

The skeleton lands in `src/integration-test/groovy/com/kousenit/QuestQueriesSpec.groovy`, annotated `@Integration` and `@Rollback`.

> [!IMPORTANT]
> The obvious move is to call `SeedData.seekTheGrail()` in Spock's `setup()` method. Do not. With `@Rollback`, the transaction begins *after* `setup()` runs, so anything `setup()` saves is committed and stays in the database for every later test, in this spec and in others. That is why the generated service specs call a `setupData()` method at the top of each feature instead. Follow the same pattern.

Replace the skeleton with this spec. Every feature method begins with `seed()`:

```groovy
package com.kousenit

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

import java.time.LocalDate

@Integration
@Rollback
class QuestQueriesSpec extends Specification {

    LocalDate today = LocalDate.now()

    // Not setup(): with @Rollback, setup() runs before the transaction begins,
    // so anything it saves is committed and leaks into the other tests.
    private void seed() {
        SeedData.seekTheGrail()
    }

    void "dynamic finders are built from property names"() {
        seed()

        expect:
        Quest.findByName('Seek the grail').tasks.size() == 9
        Task.findAllByCompleted(true).size() == 3
        Task.findAllByPriorityGreaterThan(3).size() == 4
        Task.countByCompletedAndPriorityGreaterThan(false, 3) == 2
    }

    void "finders can combine comparisons on several properties"() {
        seed()

        when: 'tasks of priority below 4 that start between yesterday and tomorrow'
        List<Task> tasks = Task.findAllByPriorityLessThanAndStartDateBetween(4, today - 1, today + 1)

        then: 'Bring out your dead qualifies on dates but not on priority'
        tasks*.name.sort() == ['Build a giant wooden rabbit',
                               'Find a shrubbery for the Knights Who Say Ni',
                               'Run away from killer rabbit',
                               'Weigh a witch against a duck']
    }

    void "list and count variants"() {
        seed()

        expect:
        Task.count() == 9
        Task.listOrderByPriority()*.priority == [1, 2, 2, 3, 3, 4, 4, 5, 5]
        Task.list(max: 2, sort: 'name').name == ['Answer the Bridgekeeper', 'Bring out your dead']
    }

    void "criteria queries compose restrictions"() {
        seed()

        when:
        List<Task> tasks = Task.withCriteria {
            ilike 'name', '%rabbit%'
            lt 'priority', 3
            order 'name', 'asc'
        }

        then:
        tasks*.name == ['Build a giant wooden rabbit', 'Run away from killer rabbit']
    }

    void "criteria can reach across associations"() {
        seed()

        when: 'quests with an incomplete task of priority 5'
        List<Quest> quests = Quest.withCriteria {
            tasks {
                eq 'completed', false
                eq 'priority', 5
            }
        }

        then:
        quests*.name == ['Seek the grail']
    }

    void "where queries are type-checked criteria in Groovy syntax"() {
        seed()

        when:
        def urgent = Task.where { priority >= 4 && completed == false }

        then:
        urgent.count() == 2
        urgent.list(sort: 'name')*.name == ['Answer the Bridgekeeper', 'Lobbest thou thy Holy Hand Grenade of Antioch']
    }

    void "where queries can be composed before they run"() {
        seed()

        given:
        def open = Task.where { completed == false }

        when: 'narrow the open tasks to the ones already overdue'
        def overdue = open.where { endDate < today }

        then:
        open.count() == 6
        overdue.count() == 0
    }

    void "findAll with a closure is a where query in disguise"() {
        seed()

        when:
        List<Task> tasks = Task.findAll { priority in [1, 5] }

        then:
        tasks*.priority.sort() == [1, 5, 5]
    }
}
```

What each group shows:

- **Dynamic finders** are methods GORM derives from the name: `findBy`, `findAllBy`, `countBy`, followed by property names, comparators such as `GreaterThan`, `LessThan`, `Between`, `Like`, and `And` or `Or` between them. `listOrderBy<Property>` sorts. They do not exist until you call them.
- **Criteria** queries use a builder closure. Restrictions compose, `order` sorts, and nesting a closure named after an association, `tasks { ... }`, becomes a join. Criteria are the tool for queries built up at runtime from optional filters.
- **Where queries** use Groovy expressions on the properties themselves and are checked at compile time, so a misspelled property is a compiler error. `Task.where { }` returns a `DetachedCriteria`: nothing runs until you call `list()`, `count()`, or `get()` on it, and you can refine it with another `where` first.
- **`findAll { }`** on a domain class is a where query that runs immediately.

> **Note:** The last test assigns the `findAll` result in a `when:` block before asserting on it. Written inline in an `expect:` block, the same call returned every task, because Spock rewrites the expressions in its assertion blocks and GORM's where-query transformation did not see the closure. Treat where-query closures like any other side-effecting call in Spock: run them in `given:` or `when:`, assert in `then:`.

### Step 4: Run it

```bash
./gradlew integrationTest --tests 'com.kousenit.QuestQueriesSpec'
```

Nine tasks, eight features, and a transaction rolled back after each one. Add a test for a query of your own, for example the tasks due in the next week, using whichever style you like least, to see how the three compare.

### Key Learning Points

- `BootStrap.init` runs at startup. Guard seed data by environment and by whether the data already exists; use `failOnError: true` so bad seed data fails loudly.
- `addTo<Collection>` builds the child, wires both sides of the association, and chains.
- With `@Rollback`, `setup()` runs before the transaction. Seed inside each feature method.
- Dynamic finders for one-liners, criteria for queries assembled at runtime, where queries for compile-time checked, composable queries.
- `logSql` and `formatSql` show you what GORM is really sending to the database.

### Checkpoint

```bash
git checkout step4-queries
```

## Lab 5: Completing the Domain Model

Quests need knights, and knights need somewhere to live. This lab adds `Knight` and `Castle`, completes the associations, and writes one more custom validator, this time one whose rule depends on *which* knight you are. By the end the model looks like this:

```
Castle 1 ──< Knight >── 1 Quest 1 ──< Task
```

### Step 1: Castle

1. Generate the class:

   ```bash
   grails create-domain-class Castle
   ```

2. Replace `grails-app/domain/com/kousenit/Castle.groovy`:

   ```groovy
   package com.kousenit

   class Castle {
       String name
       String city
       String country
       Double latitude
       Double longitude

       static hasMany = [knights: Knight]

       String toString() { name }

       static constraints = {
           name blank: false
           city blank: false
           country blank: false
           latitude nullable: true, range: -90d..90d
           longitude nullable: true, range: -180d..180d
       }
   }
   ```

   The coordinates are `Double`, not `double`, and `nullable: true`, because a castle created through the web form has no coordinates until the geocoder in Lab 7 supplies them. A primitive `double` would default to zero and put every new castle in the Gulf of Guinea.

### Step 2: Knight, and the Bridge of Death

1. Generate the class:

   ```bash
   grails create-domain-class Knight
   ```

2. Replace `grails-app/domain/com/kousenit/Knight.groovy`:

   ```groovy
   package com.kousenit

   class Knight {
       String title = 'Sir'
       String name
       String favouriteColour
       Quest quest
       Castle castle

       String toString() { "$title $name" }

       static constraints = {
           title inList: ['Sir', 'Lord', 'Lady', 'King', 'Queen']
           name blank: false
           favouriteColour nullable: true, validator: { String colour, Knight knight ->
               // The Bridgekeeper asks every knight. Only Galahad is allowed not to know.
               if (!colour && !knight.name.contains('Galahad')) {
                   return 'bridgeOfDeath'
               }
           }
           quest nullable: true
           castle nullable: true
       }
   }
   ```

   Three things are new here:

   - **`inList`** restricts a property to a fixed set of values. The scaffolding turns it into a dropdown.
   - **Plain references, not `belongsTo`.** A knight refers to a quest and a castle, but neither owns the knight. Knights between quests and knights without a castle are both legal, hence `nullable: true`, and deleting a quest does not delete its knights.
   - **A validator that returns an error code.** Lab 2's validator returned `true` or `false`. This one returns a `String`, which becomes the error code, so the message key is `knight.favouriteColour.bridgeOfDeath` instead of the generic `validator.invalid`. Returning nothing means the value is fine. Note that the closure runs even when the value is `null`; `nullable: true` turns off the nullable check but not your own.

   The rule itself: at the Bridge of Death, every knight is asked their favourite colour. Lancelot says "Blue" and crosses. Galahad says "Blue. No, yellow!" and is cast into the Gorge of Eternal Peril. So every knight must record a favourite colour, except Galahad, who is excused for never having managed a straight answer.

3. Add the message to `grails-app/i18n/messages.properties`:

   ```properties
   knight.favouriteColour.bridgeOfDeath=What... is your favourite colour? Every knight must answer the Bridgekeeper (Galahad excepted)
   ```

4. Give `Quest` its knights. In `Quest.groovy`:

   ```groovy
   static hasMany = [tasks: Task, knights: Knight]
   ```

### Step 3: Scaffolding for both

```bash
grails generate-all com.kousenit.Castle
grails generate-all com.kousenit.Knight
```

Fill in `populateValidParams` in the two new controller specs, with `name`, `city`, and `country` for a castle, and `name` plus `favouriteColour` for a knight, and replace `setupData` and the `save` test in `CastleServiceSpec` and `KnightServiceSpec` as you did in Labs 1 and 2. Five castles and five knights each; the finished versions are in the repository if you would rather read than type.

### Step 4: Test the Bridgekeeper

Replace `src/test/groovy/com/kousenit/KnightSpec.groovy`:

```groovy
package com.kousenit

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class KnightSpec extends Specification implements DomainUnitTest<Knight> {

    void "a knight needs a name and a valid title"() {
        expect:
        new Knight(name: 'Lancelot', favouriteColour: 'Blue').validate()
        !new Knight(name: ' ', favouriteColour: 'Blue').validate()
        !new Knight(title: 'Squire', name: 'Patsy', favouriteColour: 'Brown').validate()
    }

    void "quest and castle are optional, because knights wander"() {
        expect:
        new Knight(name: 'Robin', favouriteColour: 'Yellow').validate()
    }

    @Unroll
    void "#name must answer the Bridgekeeper"() {
        when:
        Knight knight = new Knight(name: name)

        then:
        !knight.validate()
        knight.errors['favouriteColour'].code == 'bridgeOfDeath'

        where:
        name << ['Lancelot the Brave', 'Robin the Not-Quite-So-Brave-as-Sir-Lancelot', 'Bedevere the Wise']
    }

    void "Galahad does not have to know his favourite colour"() {
        expect:
        new Knight(name: 'Galahad the Pure').validate()
    }

    void "Galahad may still have one, as long as he does not change his mind"() {
        expect:
        new Knight(name: 'Galahad the Pure', favouriteColour: 'Blue. No, yellow!').validate()
    }
}
```

and `CastleSpec.groovy`:

```groovy
package com.kousenit

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class CastleSpec extends Specification implements DomainUnitTest<Castle> {

    Castle doune = new Castle(name: 'Camelot', city: 'Doune', country: 'Scotland')

    void "coordinates are optional until the geocoder fills them in"() {
        expect:
        doune.validate()
        doune.latitude == null
    }

    void "coordinates must be on the planet"() {
        when:
        doune.latitude = 91
        doune.longitude = -181

        then:
        !doune.validate()
        doune.errors['latitude'].code == 'range.toobig'
        doune.errors['longitude'].code == 'range.toosmall'
    }
}
```

Run `./gradlew test`.

### Step 5: Seed the court

The castles are the places the film was shot. Doune Castle, north of Stirling, played Camelot as well as the interiors of Swamp Castle, Castle Anthrax, and the French castle. Castle Stalker, on its island near Port Appin, was Castle Aaargh. Bodiam Castle in East Sussex was Swamp Castle from the outside.

1. Add a second method to `SeedData.groovy`. The coordinates are hard-coded so that the application never needs the network to start:

   ```groovy
   static List<Castle> theCourt(Quest quest) {
       Castle camelot = new Castle(name: 'Camelot', city: 'Doune', country: 'Scotland',
               latitude: 56.1853d, longitude: -4.0509d)
               .addToKnights(title: 'King', name: 'Arthur', favouriteColour: 'Blue', quest: quest)
               .addToKnights(name: 'Lancelot the Brave', favouriteColour: 'Blue', quest: quest)
               .addToKnights(name: 'Galahad the Pure', quest: quest)
               .addToKnights(name: 'Robin the Not-Quite-So-Brave-as-Sir-Lancelot', favouriteColour: 'Yellow', quest: quest)
               .addToKnights(name: 'Bedevere the Wise', favouriteColour: 'Green', quest: quest)
               .save(failOnError: true)
       Castle aaargh = new Castle(name: 'Castle Aaargh', city: 'Port Appin', country: 'Scotland',
               latitude: 56.5695d, longitude: -5.3870d)
               .save(failOnError: true)
       Castle swamp = new Castle(name: 'Swamp Castle', city: 'Robertsbridge', country: 'England',
               latitude: 51.0023d, longitude: 0.5436d)
               .addToKnights(title: 'Lord', name: 'of Swamp Castle', favouriteColour: 'Huge tracts of land')
               .save(failOnError: true)
       [camelot, aaargh, swamp]
   }
   ```

   Saving a castle cascades to its knights, because `Castle hasMany knights`. Each knight also points at the quest, so after this runs, `quest.knights` has five members without anyone calling `addToKnights` on the quest. GORM maintains both sides.

2. Call it from `BootStrap.groovy`:

   ```groovy
   Quest quest = SeedData.seekTheGrail()
   SeedData.theCourt(quest)
   ```

3. Run the app. Camelot's show page lists five knights, the quest's show page lists the same five, and **New Knight** has a title dropdown. Try to save a knight with no favourite colour, then try again with a name containing Galahad.

### Key Learning Points

- `inList` for enumerated values; the scaffolding renders a select.
- Associations without `belongsTo` are plain references: optional, and not cascaded on delete.
- A validator closure can return an error code of your own, which gives it its own message key.
- Validators run for `null` values; `nullable: true` only disables the built-in nullable check.
- Use wrapper types (`Double`) for optional numeric properties so that "unknown" is `null`, not zero.
- When both sides of an association are set during seeding, GORM keeps them consistent.

### Checkpoint

```bash
git checkout step5-model
```

## Lab 6: Scaffolding with @Scaffold

Every `generate-all` so far produced a 99-line controller, a 17-line service interface, four GSP files, and two test skeletons. That is **static scaffolding**: code you own, can read, and must maintain. Grails 7 adds a second option, the `@Scaffold` annotation, which does the same work at compile time and runtime and leaves nothing in your source tree but an empty class. This lab converts `Castle` to it, so you can compare the two side by side.

### Step 1: Measure what you are about to delete

```bash
wc -l grails-app/controllers/com/kousenit/CastleController.groovy \
      grails-app/services/com/kousenit/CastleService.groovy \
      grails-app/views/castle/*.gsp \
      src/test/groovy/com/kousenit/CastleControllerSpec.groovy
```

Five hundred and thirty-five lines, give or take, none of which you wrote.

### Step 2: Delete it

```bash
git rm grails-app/controllers/com/kousenit/CastleController.groovy \
       grails-app/services/com/kousenit/CastleService.groovy \
       src/test/groovy/com/kousenit/CastleControllerSpec.groovy
git rm -r grails-app/views/castle
```

Keep `CastleServiceSpec` in `src/integration-test`; it will need one small change.

### Step 3: Generate the annotated versions

```bash
grails generate-scaffold-all com.kousenit.Castle
```

The command writes two files. The controller:

```groovy
package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold
import grails.plugin.scaffolding.RestfulServiceController

@Scaffold(RestfulServiceController<Castle>)
class CastleController {}
```

and the service:

```groovy
package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold

@Scaffold(Castle)
class CastleService {
}
```

That is the whole Castle web layer now. How it works:

- `@Scaffold` is processed by a Grails **AST transformation** at compile time. `@Scaffold(Castle)` on the service makes the class extend `GormService<Castle>`, which implements `get`, `list`, `count`, `save`, and `delete` against GORM. `@Scaffold(RestfulServiceController<Castle>)` on the controller makes it extend `RestfulController<Castle>` with the seven CRUD actions, and routes every data operation through the `CastleService` bean.
- The simpler `@Scaffold(Castle)` on a controller also works; it extends `RestfulController` and talks to the domain class directly, with no service in between.
- **Views** are generated at runtime, from the same templates `generate-all` uses, whenever no GSP exists for the action. If you later want to customize one page, run `grails generate-views com.kousenit.Castle` and edit only the file you need. You will do exactly that for the map in Lab 8.
- `RestfulController` responds to content negotiation. Try `curl -H "Accept: application/json" http://localhost:8080/castle/show/1` and you get the castle as JSON, knights included, without writing a JSON view.

### Step 4: Fix the tests

1. The old controller spec mocked the service interface method by method, which no longer applies. Replace `src/test/groovy/com/kousenit/CastleControllerSpec.groovy` with a test that proves the transformation happened:

   ```groovy
   package com.kousenit

   import grails.rest.RestfulController
   import grails.testing.web.controllers.ControllerUnitTest
   import spock.lang.Specification

   class CastleControllerSpec extends Specification implements ControllerUnitTest<CastleController> {

       void "the annotation turns an empty class into a RestfulController for Castle"() {
           expect:
           controller instanceof RestfulController
           controller.resource == Castle
           ['index', 'show', 'create', 'save', 'edit', 'update', 'delete'].every { controller.respondsTo(it) }
       }
   }
   ```

2. `GormService.count` takes a `Map` of query arguments, where the data service interface's `count()` took none. In `CastleServiceSpec`, change both `castleService.count()` calls to `castleService.count([:])`.

3. Run `./gradlew test` and `./gradlew integrationTest --tests 'com.kousenit.CastleServiceSpec'`.

### Step 5: Run it

Start the application and visit http://localhost:8080/castle. The list, the show page with its knights, the create form with the blank-name validation, and the edit page all behave as before, with no GSP files on disk. Add a castle, then check the JSON endpoint.

### When to use which

Static scaffolding is a starting point you edit: the generated controller is readable Groovy, and most real Grails applications begin that way and then diverge. `@Scaffold` is for the parts of an application that stay standard: admin screens, reference data, anything where the generated behavior is the behavior you want. You can mix them freely, as this project now does, and you can add or override individual actions on an annotated controller when one page needs something special.

### Key Learning Points

- `@Scaffold` is a compile-time AST transformation. The empty class really does become a `RestfulController` or `GormService` subclass, which is why a unit test can check `instanceof`.
- `generate-scaffold-all` writes the two annotated classes; `generate-all` writes the full static version. Both come from the scaffolding plugin.
- Runtime views fall back to the same templates, and `generate-views` lets you take over one page at a time.
- `RestfulController` gives JSON and XML for free through content negotiation.

### Checkpoint

```bash
git checkout step6-scaffold
```

## Lab 7: A Geocoder Service

Services are where Grails puts business logic: Spring beans in `grails-app/services`, injected into controllers and other services by name, transactional by default. This lab writes one that calls an external web API to find a castle's coordinates, tests it without the network, tests it with the network, and hooks it into the scaffolded `CastleService` so that any castle created through the web form is geocoded on save.

The API is [Open-Meteo's geocoding endpoint](https://open-meteo.com/en/docs/geocoding-api). It is free, needs no key, and answers a town name with JSON:

```
https://geocoding-api.open-meteo.com/v1/search?name=Doune&count=1
```

```json
{"results":[{"name":"Doune","latitude":56.18995,"longitude":-4.05288,"country":"United Kingdom","admin1":"Scotland", ...}]}
```

When nothing matches, the response has no `results` key at all.

### Step 1: Create the service

```bash
grails create-service Geocoder
```

That makes `grails-app/services/com/kousenit/GeocoderService.groovy` and a unit test skeleton. The generated class is annotated `@Transactional`. Remove that: this service touches no database, and a transaction around an HTTP call is a waste.

### Step 2: Exercise: implement the lookup

Give the service two methods with these signatures:

```groovy
package com.kousenit

import groovy.json.JsonSlurper

class GeocoderService {

    static final String BASE = 'https://geocoding-api.open-meteo.com/v1/search'

    /** Fill in latitude and longitude from the castle's city, if the API knows it. Returns the castle. */
    Castle fillInLatLng(Castle castle) {
        // TODO
    }

    /** The first matching place for a town name, as a Map, or null if there is none. */
    Map lookup(String city) {
        // TODO
    }
}
```

Two methods rather than one, because the split is what makes the service testable: `lookup` is the only method that touches the network, so a test can replace just that one. `JsonSlurper` is in `groovy-json`, which Grails includes. `String.toURL()` and `URL.text` or `JsonSlurper.parse(URL)` come from the Groovy JDK. Encode the town name; "Port Appin" has a space in it.

When you have it, compare with this version:

```groovy
    Castle fillInLatLng(Castle castle) {
        Map place = lookup(castle.city)
        if (place) {
            castle.latitude = place.latitude as Double
            castle.longitude = place.longitude as Double
        }
        castle
    }

    Map lookup(String city) {
        String url = "$BASE?name=${URLEncoder.encode(city, 'UTF-8')}&count=1"
        Map response = new JsonSlurper().parse(url.toURL()) as Map
        (response.results as List<Map>)?.find()
    }
```

`find()` with no argument returns the first element, or `null` for an empty or missing list, which is exactly the contract we want.

### Step 3: Unit test with a Spy

Replace `src/test/groovy/com/kousenit/GeocoderServiceSpec.groovy`:

```groovy
package com.kousenit

import grails.testing.gorm.DomainUnitTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class GeocoderServiceSpec extends Specification
        implements ServiceUnitTest<GeocoderService>, DomainUnitTest<Castle> {

    Castle camelot = new Castle(name: 'Camelot', city: 'Doune', country: 'Scotland')

    void "coordinates are copied from the first result"() {
        given: 'a service whose lookup never touches the network'
        GeocoderService geocoder = Spy(GeocoderService) {
            lookup('Doune') >> [name: 'Doune', latitude: 56.18995, longitude: -4.05288]
        }

        when:
        geocoder.fillInLatLng(camelot)

        then:
        camelot.latitude == 56.18995d
        camelot.longitude == -4.05288d
        camelot.validate()
    }

    void "an unknown town leaves the coordinates alone"() {
        given:
        GeocoderService geocoder = Spy(GeocoderService) {
            lookup(_) >> null
        }
        Castle anthrax = new Castle(name: 'Castle Anthrax', city: 'Nowheresville', country: 'Scotland')

        when:
        geocoder.fillInLatLng(anthrax)

        then:
        anthrax.latitude == null
        anthrax.longitude == null
    }

    void "the service returns the castle so calls can chain"() {
        given:
        GeocoderService geocoder = Spy(GeocoderService) { lookup(_) >> null }

        expect:
        geocoder.fillInLatLng(camelot).is(camelot)
    }
}
```

A Spock **Spy** wraps a real object. Methods you stub, here `lookup`, return what you say; everything else runs the real code. So `fillInLatLng` is tested for real, with the network call replaced. The spec implements both `ServiceUnitTest` for the service and `DomainUnitTest<Castle>` so that `new Castle(...)` and `validate()` work.

### Step 4: Integration test against the real API

Create `src/integration-test/groovy/com/kousenit/GeocoderServiceLiveSpec.groovy`:

```groovy
package com.kousenit

import grails.testing.mixin.integration.Integration
import spock.lang.IgnoreIf
import spock.lang.Specification

@Integration
@IgnoreIf({ env.OFFLINE })
class GeocoderServiceLiveSpec extends Specification {

    GeocoderService geocoderService

    void "Doune is where we left it"() {
        when:
        Map place = geocoderService.lookup('Doune')

        then:
        place.country == 'United Kingdom'
        (place.latitude - 56.19).abs() < 0.05
        (place.longitude - -4.05).abs() < 0.05
    }

    void "a town the Bridgekeeper has never heard of returns nothing"() {
        expect:
        geocoderService.lookup('Nowheresville') == null
    }
}
```

The `@Integration` test gets the real `GeocoderService` bean injected by name. `@IgnoreIf({ env.OFFLINE })` skips both tests when an `OFFLINE` environment variable is set, so the build still passes on conference wifi:

```bash
OFFLINE=1 ./gradlew integrationTest
```

### Step 5: Geocode on save

The scaffolded `CastleService` from Lab 6 extends `GormService<Castle>`, so its `save` can be overridden like any method. Edit `grails-app/services/com/kousenit/CastleService.groovy`:

```groovy
package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold

@Scaffold(Castle)
class CastleService {

    GeocoderService geocoderService

    @Override
    Castle save(Castle castle) {
        if (castle.latitude == null || castle.longitude == null) {
            geocoderService.fillInLatLng(castle)
        }
        super.save(castle)
    }
}
```

`GeocoderService geocoderService` is injected by Spring because the property name matches the bean name; no annotation needed. The `null` check means the seed castles, which arrive with coordinates, never trigger a network call, and startup stays offline.

Run everything:

```bash
./gradlew test
./gradlew integrationTest
```

### Step 6: Try it

Start the app, go to **Castles → New Castle**, and create *Castle Anthrax* in *Doune*, *Scotland*, leaving the coordinates blank. The show page comes back with latitude 56.18995 and longitude -4.05288. The JSON view agrees:

```bash
curl -H "Accept: application/json" http://localhost:8080/castle/show/4
```

### Key Learning Points

- Services are Spring beans injected by name. Drop `@Transactional` when there is no database work.
- Split the network call into its own method so a Spock `Spy` can replace it and test the rest for real.
- `@IgnoreIf` and `@Requires` keep tests that need the outside world from breaking the build when it is not there.
- A `@Scaffold` service is a real subclass of `GormService`; override `save` to add behavior.
- Design seed data so startup never depends on the network.

### Checkpoint

```bash
git checkout step7-geocoder
```

## Lab 8: Mapping the Castles

The castles have coordinates; let us see them. This lab puts an [OpenStreetMap](https://www.openstreetmap.org) map on the castle list page using [Leaflet](https://leafletjs.com), the standard open-source JavaScript mapping library. Along the way you take over one scaffolded view, add an action to a `@Scaffold` controller, and serve a JavaScript library from a **webjar** so the page has no CDN dependency.

### Step 1: Leaflet as a webjar

[WebJars](https://www.webjars.org) package npm libraries as Maven artifacts. Spring Boot serves anything under `META-INF/resources/webjars` on the classpath at `/webjars/**`, and Grails inherits that. Add to the `dependencies` block of `build.gradle`:

```groovy
runtimeOnly "org.webjars.npm:leaflet:1.9.4"
```

After the next restart, `http://localhost:8080/webjars/leaflet/1.9.4/dist/leaflet.js` serves the library from the jar. The map tiles themselves still come from OpenStreetMap's servers, so the page needs the network to show a map, but not to load.

### Step 2: Take over the list view

Lab 6 left `Castle` with no GSP files; the scaffolding renders them from templates at runtime. To customize the list page, generate the static versions and keep only the one you want:

```bash
grails generate-views com.kousenit.Castle
rm grails-app/views/castle/create.gsp grails-app/views/castle/edit.gsp grails-app/views/castle/show.gsp
```

Now `index.gsp` is yours and the other three pages stay dynamic. Open `grails-app/views/castle/index.gsp`.

1. At the very top, before `<!DOCTYPE html>`, import the JSON converter for use later:

   ```jsp
   <%@ page import="grails.converters.JSON" %>
   ```

2. In `<head>`, after the `<title>`, load Leaflet from the webjar and give the map a height:

   ```html
   <link rel="stylesheet" href="${createLink(uri: '/webjars/leaflet/1.9.4/dist/leaflet.css')}"/>
   <script src="${createLink(uri: '/webjars/leaflet/1.9.4/dist/leaflet.js')}"></script>
   <style>#map { height: 420px; }</style>
   ```

   `createLink(uri:)` prefixes the context path, so the page keeps working if the app is ever deployed under `/holygrails`.

3. Above the `<f:table ...>` tag, add the map container:

   ```html
   <div id="map" class="mb-3 border rounded"></div>
   ```

4. Just before `</body>`, add the script that draws the markers. The marker data comes from the model as a Groovy list, rendered into the page as JSON:

   ```html
   <script>
       const markers = ${raw((markers as JSON).toString())};
       // Wait for the stylesheets: Leaflet needs the container's final size to fit the bounds.
       window.addEventListener('load', () => {
           const map = L.map('map');
           L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
               maxZoom: 18,
               attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
           }).addTo(map);
           const group = L.featureGroup(markers.map(m =>
               L.marker([m.lat, m.lng]).bindPopup(
                   `<strong><a href="${'$'}{m.url}">${'$'}{m.name}</a></strong><br>${'$'}{m.city}<br>${'$'}{m.knights} knight(s)`)
           )).addTo(map);
           if (markers.length) {
               map.fitBounds(group.getBounds().pad(0.2));
           } else {
               map.setView([56.19, -4.05], 6);
           }
       });
   </script>
   ```

   The `load` listener matters: the page's stylesheets, including Leaflet's, arrive after this script runs, and `fitBounds` computed against a container with no height yet produces a map zoomed out to the whole world. Waiting for `load` gives Leaflet the real size.

   Two GSP details here. `raw()` stops GSP from HTML-encoding the JSON, which would turn every quote into `&quot;`. And a JavaScript template literal's `${...}` looks exactly like a GSP expression, so it is written as `${'$'}{m.name}`: GSP evaluates `${'$'}` to a literal dollar sign and leaves the braces alone.

### Step 3: Supply the markers from the controller

The list page now expects a `markers` entry in the model. The `@Scaffold` controller can override the scaffolded `index` action to add it. Edit `grails-app/controllers/com/kousenit/CastleController.groovy`:

```groovy
package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold
import grails.plugin.scaffolding.RestfulServiceController

@Scaffold(RestfulServiceController<Castle>)
class CastleController {

    /** The scaffolded index, plus one extra model entry: every castle that knows where it is. */
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond listAllResources(params), model: [castleCount: countResources(), markers: markers()]
    }

    private List<Map> markers() {
        Castle.findAllByLatitudeIsNotNullAndLongitudeIsNotNull().collect { Castle castle ->
            [name: castle.name, city: castle.city, lat: castle.latitude, lng: castle.longitude,
             knights: castle.knights.size(), url: createLink(action: 'show', id: castle.id)]
        }
    }
}
```

The first two lines of `index` are what `RestfulController` does by itself: `listAllResources` and `countResources` are its protected methods, and because `@Scaffold` made this class a real subclass, you can call them. The third model entry is ours. `respond` renders the `castleList` for the table as before, and the whole model is available to the GSP.

The finder `findAllByLatitudeIsNotNullAndLongitudeIsNotNull` is a mouthful, but it reads as what it does, and it keeps castles that failed geocoding off the map rather than dropping them in the Atlantic at 0, 0.

### Step 4: Look

Restart and open http://localhost:8080/castle. Three markers in Britain: Camelot and Castle Aaargh in Scotland, Swamp Castle on the south coast of England. Click a marker for the castle name, town, and knight count, with a link to its show page. If you created Castle Anthrax in Lab 7, it sits on top of Camelot, since both are Doune Castle.

### Key Learning Points

- Webjars put front-end libraries on the classpath; Spring Boot serves them at `/webjars/**` with no configuration.
- `generate-views` lets you take over a single scaffolded page while the rest stay dynamic.
- A `@Scaffold` controller can override or add actions, and call `RestfulController`'s protected helpers.
- `raw()` disables GSP's HTML encoding when you mean to emit markup or JSON. Use it only for data you trust.
- `${'$'}` is how a GSP emits a literal dollar sign for JavaScript template literals.

### Checkpoint

```bash
git checkout step8-map
```

## Lab 9: What's New in Grails 8 (and 7)

Grails 8.0.0 was released on 11 October 2026, the day before the talk these labs accompany. Grails 7.0.0 had shipped on 28 October 2025 as the first release after Grails graduated to an Apache Software Foundation top-level project, and these labs were written on 7.2.4 and moved to 8.0.0 the day it came out. This lab has one piece of code, a functional test that uses the most visible testing feature of the 7 line, and then a tour: what changed in 8, what moving this application took, an upgrade checklist for 7 to 8, and, for anyone bringing a Grails 5 or 6 application forward, what changed in 7. Everything here is taken from the 8.0.0 user guide's *What's new* and *Upgrading* chapters, or was verified while building and porting these labs.

### Step 1: A functional test in a containerized browser

Lab 1 mentioned `ContainerGebSpec`, the Grails 7 way to run [Geb](https://groovy.apache.org/geb/) browser tests: Testcontainers starts a browser in Docker, points it at the running application, and your test drives it. The starter app came with one such test for the welcome page. Here is one for the map from Lab 8. Create `src/integration-test/groovy/com/kousenit/CastleMapSpec.groovy`:

```groovy
package com.kousenit

import grails.plugin.geb.ContainerGebSpec
import grails.testing.mixin.integration.Integration
import org.apache.grails.testing.cleanup.core.DatabaseCleanup

@Integration
@DatabaseCleanup
class CastleMapSpec extends ContainerGebSpec {

    def setup() {
        Castle.withNewTransaction {
            SeedData.theCourt(SeedData.seekTheGrail())
        }
    }

    void 'the castle list shows every castle with coordinates on the map'() {
        when: 'visiting the castle list'
        go '/castle'

        then: 'the page is the scaffolded list'
        title == 'Castle List'

        and: 'Leaflet has drawn one marker per castle'
        waitFor { $('#map .leaflet-marker-icon').size() == 3 }

        when: 'clicking the first marker'
        $('#map .leaflet-marker-icon', 0).click()

        then: 'its popup names the castle and links to it'
        waitFor { $('.leaflet-popup-content').text().contains('knight(s)') }
        $('.leaflet-popup-content a').text() in ['Camelot', 'Castle Aaargh', 'Swamp Castle']
    }
}
```

Three things to notice:

- **`BootStrap` does not run in the test environment**, so the spec seeds its own castles in `setup()`, inside a transaction, because a browser request is not going to run inside the test's transaction.
- **`@DatabaseCleanup`** truncates every table after each test. It is the Grails 7 answer to tests that commit data and therefore cannot use `@Rollback`. It needs the cleanup module for your database in `build.gradle`:

  ```groovy
  integrationTestImplementation "org.apache.grails:grails-testing-support-dbcleanup-h2"
  ```

- **`waitFor`** is Geb's way of waiting for JavaScript. The markers appear only after Leaflet runs on the page's `load` event.

Run it with Docker running:

```bash
./gradlew integrationTest --tests 'com.kousenit.CastleMapSpec'
```

Fifteen seconds or so: container start, application start, one real browser session. The browser recording and reporting options in the Geb plugin README let you keep a video of a failing test. Grails 8 adds a `grails-testing-support-latency` module that injects random delays into the application under test, so a functional test that only passes when the server is quick fails every time instead of once a week.

### Step 2: What changed in Grails 8

#### The foundation

| | Grails 7.2.4 | Grails 8.0.0 |
|---|---|---|
| Java | 17+ (Gradle 8.14 capped the build JDK at 24) | **21+**; 25 works, in IntelliJ too |
| Groovy | 4.0.33 | **5.1.3** |
| Spring Boot | 3.5.16 | **4.1.1** |
| Spring Framework | 6.2.19 | **7.0.9** |
| Jackson | 2.x (`com.fasterxml.jackson`) | **3.x** (`tools.jackson`) |
| Tomcat / Servlet | 10.1 / 6.0 | **11 / 6.1** |
| Hibernate | 5.6.15 | 5.6.15 by default; **7** as a Forge option |
| Spock | 2.3-groovy-4.0 | **2.4-groovy-5.0** |
| Gradle | 8.14.5 (9 unsupported) | **9.8.0** (9.7 minimum) |

The Spring Boot 4 move is the one with the longest tail: the autoconfigure module was split into domain-specific modules, several starters were renamed (`spring-boot-starter-web` is now `spring-boot-starter-webmvc`, `-aop` is `-aspectj`), Jackson 3 lives in a new package, and `HttpStatus.MOVED_TEMPORARILY` is gone. The Grails upgrade guide lists each one and links to the Spring Boot migration guides. For a WAR deployed to an external container, `providedRuntime "org.springframework.boot:spring-boot-starter-tomcat-runtime"` replaces the old "mark Tomcat as provided" idiom.

#### The change most likely to bite: nullable by default

Every Grails before 8 applied an implicit `nullable: false` to a domain property that declared no constraint. Grails 8 adopts the JPA, Spring Data and Bean Validation rule instead: **an unconstrained property is nullable**. Port this application as-is and a `Task` with no name and no quest validates, saves, and gets a nullable column from `schemaExport`; the `TaskSpec` test from Lab 3 that expects `task.quest = null` to fail validation goes red.

Two fixes. Declare `nullable: false` wherever you mean it, which is what a new application should do, or restore the old rule for the whole application in `application.yml`, which is what this one does:

```yaml
grails:
  gorm:
    default:
      nullable: false
```

`DomainUnitTest` and `DataTest` read the same setting, so unit tests and the running application agree. (They did not in 8.0.0-RC2; that was fixed before the release, in apache/grails-core#16466.) Command objects are unaffected: a `Validateable` property is still required by default, except for one imported with `importFrom` from a domain class, which takes the domain default. Review your schema before running a migration tool: columns that used to be `NOT NULL` will not be.

#### What moving this application took

Beyond the nullable setting, the port from 7.2.4 to 8.0.0 came down to this, and every domain class, controller, service, view and test other than one line is unchanged:

1. **`RestfulController.index` now supplies a `Long` count** where 7 supplied an `Integer`. The compiled JSON view from Lab 11 declared `Integer questCount` in its `model` block and rejected the `Long` at runtime; it now declares `Long`.
2. **The Forge 8 starter is different**, and the simplest port is to adopt it. The layout is SiteMesh 3 (`grails-sitemesh3` replaces `grails-layout`) with a navbar and dark mode; the welcome page lists every servlet filter in the order the container applies them; the i18n bundles were regenerated; `logback-spring.xml` is no longer generated (Spring Boot's logging defaults apply, and `logging.level` entries in `application.yml` tune them); the `grails.mime.types` block is gone from `application.yml` because the framework now supplies those defaults; and `htmlcodec: xml` is the default, so it is no longer written.
3. **`build.gradle` lost two things.** The `console` configuration is gone: Grails commands moved out of the runtime plugin jars into companion `-cli` artifacts (`grails-core-cli`, `grails-data-hibernate7-dbmigration-cli`, and so on) that the Gradle plugin provisions into a `grailsCli` configuration by itself, so `grails console` and `schema-export` still work and nothing CLI-only ends up in your jar. And the configuration-cache workaround for `buildProperties` from the Grails 7 version of Lab 0 is unnecessary; the Grails 8 build is configuration-cache clean. `testcontainers-spock` is the new coordinate for Testcontainers' Spock module, and `compileJava.options.release` is 21.
4. **The Spring dependency-management plugin is no longer applied.** The Grails BOM is contributed as a Gradle `platform()`, which takes part in normal conflict resolution, so a transitive dependency can win over a BOM default. `gradle.properties` overrides such as `spring-boot.version=` still work, as strict constraints; `enforcedPlatform` gets the old "BOM always wins" behaviour back.

The `groovy-datetime` dependency from Lab 2 is still needed: Groovy 5 is modular exactly as Groovy 4 was.

#### Features worth knowing

- **Security headers by default.** Every response carries `X-Content-Type-Options: nosniff`, `X-Frame-Options: SAMEORIGIN`, `Referrer-Policy: strict-origin-when-cross-origin` and `X-XSS-Protection: 0`; HSTS and a Content Security Policy are available but off. Everything is under `grails.security.headers.*`. Open the browser's network tab on the welcome page and look.
- **Plugin beans register before Spring Boot auto-configuration**, so a plugin bean wins over a `@ConditionalOnMissingBean` default. Plugins and unit tests declare beans through a `beanRegistrar()` hook returning a Spring `BeanRegistrar`; the `doWithSpring` bean-builder DSL is deprecated.
- **Static compilation for a whole application.** `grails { compileStatic { controllers = true; services = true; tagLibs = true } }` in `build.gradle` opts every artefact of a type into `@GrailsCompileStatic` without annotating each class, a statically compiled controller can call tag libraries, and tag calls compile to direct invocations. Forge has a `grails-compile-static` feature that generates all of it.
- **URL mappings.** `"/$controller"(resources: '*')` maps every controller as a REST resource in one line, and links resolve a controller's namespace without being told it.
- **GORM locking.** `book.refresh(lock: true)` reloads under a pessimistic write lock, `Book.lock(id, refresh: true)` does the same by identifier, and both accept a `jakarta.persistence.LockModeType`. `mutex` now reloads under the lock instead of failing on a stale version.
- **JSON dates render as Spring Boot renders them**, `java.time` types included, so a Grails API and a Spring Boot API return the same JSON for the same values.
- **Testing.** `httpPostForm(...)` joins `HttpClientSupport` for form-encoded requests, and the latency module from Step 1.
- **Messages come from Spring Boot's `MessageSourceAutoConfiguration`**, so `spring.messages.*` works, and plugin bundles must be namespaced on the plugin name.
- **Ahead-of-time.** Spring AOT processing (`apply plugin: 'org.springframework.boot.aot'`) and GraalVM native images are supported, and on JDK 25 `grails { aotCache { enabled = true } }` trains a JDK AOT cache so the next start skips class loading and linking.
- **The banner is coloured** and names the servlet container; you saw it in Lab 0.
- **Agent skills ship with the release**: `org.apache.grails.skills:grails-developer` for building applications and `grails-8-upgrade` for moving one from 7, applied with the SkillsJars Gradle plugin and `./gradlew extractSkillsJars`.
- **Hibernate 7** is a Forge option (`--data=hibernate7`), on its own `grails-hibernate7-bom`. Hibernate removed its legacy `Criteria` API in version 6, which is part of why Grails 7 stayed on 5.6; the Hibernate 7 module keeps the `withCriteria` DSL from Lab 4 and re-implements it on JPA Criteria.
- **Also:** a `gorm-async` Forge feature, Quartz no longer refuses to start over a trigger that can never fire, the Mail plugin separates `overrideToAddress` from `overrideFromAddress`, and GORM for MongoDB gains TTL indexes, Spring Data MongoDB interoperability and CRaC checkpoints.

### Step 3: Upgrading from 7 to 8

In the order the guide suggests, with the steps this application actually needed marked:

1. **Let a tool find the configuration changes.** Add `runtimeOnly "org.springframework.boot:spring-boot-properties-migrator"` for the duration, boot once, and read the warnings. Or hand the guide to an agent: with the application still on 7, `skill "org.apache.grails.skills:grails-8-upgrade:8.0.0"` under the SkillsJars plugin extracts a checklist your coding agent follows.
2. **JDK 21 or later and Gradle 9.8.** `./gradlew wrapper --gradle-version 9.8.0`, then check the Gradle 9 upgrade notes for your own build logic. *(Needed here: wrapper, `.sdkmanrc`.)*
3. **Generate a fresh 8.0.0 application from Forge** with the same features and diff `build.gradle`, `gradle.properties` and `application.yml` against yours. *(Needed here: this is how the port was done; see Step 2.)*
4. **Apply the Spring Boot 4 renames**: starters, autoconfigure imports, `spring-boot-starter-tomcat-runtime` for WARs. `spring-boot-starter-classic` exists as a stopgap that pulls in everything. *(Not needed here.)*
5. **Jackson 3** is `tools.jackson.*`; `jackson-bom.version` now sets Jackson 3 and `jackson-2-bom.version` sets Jackson 2, so a Jackson 2 version under the old property name fails the build. *(Not needed here.)*
6. **Decide what nullable means.** Add `grails.gorm.default.nullable: false`, or declare `nullable: false` where it matters, before you run a schema migration. *(Needed here.)*
7. **Check the Groovy 5 behaviour changes** the guide lists: assigning a static field from a static closure needs the class name, probing a `ConfigObject` for a missing key behaves differently, and a getter on a map no longer hides behind a map entry. *(Not hit here.)*
8. **Commands.** Classes in `grails-app/commands` import `org.apache.grails.core.cli.ApplicationCommand` instead of `grails.dev.commands.ApplicationCommand`; a plugin that has not republished its commands as a `-cli` artifact works with `grails { legacyCommandSupport = true }`.
9. **Plugins.** Every plugin needs an 8 build. The guide names what breaks: Groovy 4 trait and AST-transform bytecode, Jackson 2 dataformats, pre-Apache `org.grails` coordinates, and Spring Framework 7's `HttpHeaders` change.
10. **Run the tests**, including a `DomainUnitTest` for every domain class with a property you expect to be required. Then run them in a container.

### Step 4: What changed in Grails 7, for anyone coming from 5 or 6

The guide calls the path incremental: get to 7 first, then 8. The 7 move is the one that touches application code.

| | Grails 6 | Grails 7 |
|---|---|---|
| Java | 11+ | **17+** |
| Groovy | 3.0 | **4.0** |
| Spring Boot | 2.7 | **3.5** |
| Jakarta EE | `javax.*` | **`jakarta.*`** |
| Gradle | 7.6 | **8.14** |

- **The `javax` to `jakarta` move** changes every `javax.servlet` and `javax.persistence` import. The guide suggests the Nebula `jakartaee-migration` Gradle plugin for projects with a lot of them.
- **Every artifact moved to `org.apache.grails`**, with one `grails-bom` managing versions, so dependency lines lose their version numbers: `org.grails:grails-core` became `org.apache.grails:grails-core`, `org.grails.plugins:hibernate5` became `org.apache.grails:grails-data-hibernate5`, the Spring Security and Quartz plugins moved into the core repository and share the Grails version, and the asset pipeline is `cloud.wondrify:asset-pipeline-grails`. `RENAME.md` in the grails-core repository has the full map, and `etc/bin/rename_gradle_artifacts.sh` there rewrites a project's Gradle files. In `gradle.properties`, `grailsVersion` is the only version you set.
- **Micronaut left the default stack.** Grails 4 through 6 ran it as the parent application context; `grails-micronaut` is an opt-in for projects that used it. It now lives in its own Apache repository.
- **The classic CLI and `grailsw` came back**, delegating to Gradle; Forge generates projects and has the HTTP API from Lab 0. Test dependencies came off the production classpath, `stop-app` switched to a PID file, and Groovy's invokedynamic is off for Grails compiles unless `grails { indy = true }`.
- **The features you have used in these labs arrived in 7:** `@Scaffold` (Lab 6) and the `create-scaffold-*` commands, `ContainerGebSpec` (this lab), `HttpClientSupport` (7.1, Lab 11), `@DatabaseCleanup`, custom test phases, the audit annotations `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy` and `@LastModifiedBy` (7.1) as an alternative to the `dateCreated` convention from Lab 2, the startup banner, built-in external configuration, ISO-8601 JSON dates, `formActionSubmit`, `g:flashMessages`, CSRF tokens from `g:form`, Bootstrap 5.3 scaffolding, URL mapping `group`s and greedy `$id+` variables (7.1), and SiteMesh 3 (7.2).
- **Watch for the Groovy 4 behaviour changes** the guide lists: primitive `boolean` properties no longer generate `getX()`, `DELEGATE_FIRST` closure resolution order changed, and public fields now appear in `MetaClass` properties.

### Key Learning Points

- Grails 8 is Java 21, Groovy 5, Spring Boot 4.1 on Spring Framework 7, Gradle 9.8, and Jackson 3; Hibernate 5.6 by default with 7 as a Forge option.
- Unconstrained domain properties are nullable by default; `grails.gorm.default.nullable: false` restores the old rule, and the unit tests honour it.
- Porting a 7.2.4 application took a config key, a `Long` in a JSON view, and adopting the Forge 8 build and layout.
- Grails 7 is `jakarta.*`, `org.apache.grails` coordinates and one BOM; get there first, then 8.

### Checkpoint

```bash
git checkout step9-grails7
```

The tag keeps the name it was given when this lab was about Grails 7.

## Lab 10: Enemies: Inheritance in GORM

Every quest has opposition. The Black Knight, the Rabbit of Caerbannog, and the Bridgekeeper have little in common beyond a name, a location, and a quest to obstruct, and each has properties and behavior of its own. That is an inheritance hierarchy, and GORM maps one with no configuration at all. This lab builds it, looks at the table it produces, and shows how queries behave across the hierarchy.

### Step 1: The base class

```bash
grails create-domain-class Enemy
```

Replace `grails-app/domain/com/kousenit/Enemy.groovy`:

```groovy
package com.kousenit

class Enemy {
    String name
    String location
    boolean defeated

    static belongsTo = [quest: Quest]

    String toString() { name }

    /** What this enemy says when a knight approaches. Subclasses override. */
    String challenge() { "None shall pass" }

    static constraints = {
        name blank: false
        location blank: false
    }
}
```

An ordinary domain class, with one ordinary method. Nothing marks it as a base class; the subclasses do that.

### Step 2: Three kinds of enemy

Create the subclasses with the CLI, then replace each one.

```bash
grails create-domain-class BlackKnight
grails create-domain-class RabbitOfCaerbannog
grails create-domain-class Bridgekeeper
```

`BlackKnight.groovy`:

```groovy
package com.kousenit

class BlackKnight extends Enemy {
    int limbsRemaining = 4

    @Override
    String challenge() {
        limbsRemaining > 0 ? "None shall pass" : "All right, we'll call it a draw"
    }

    boolean isMerelyAFleshWound() { limbsRemaining > 0 }

    static constraints = {
        limbsRemaining range: 0..4
    }
}
```

`RabbitOfCaerbannog.groovy`:

```groovy
package com.kousenit

class RabbitOfCaerbannog extends Enemy {
    int knightsEaten = 0
    String weakness = 'Holy Hand Grenade of Antioch'

    @Override
    String challenge() { 'Look at the bones!' }

    static constraints = {
        knightsEaten min: 0
    }
}
```

`Bridgekeeper.groovy`:

```groovy
package com.kousenit

class Bridgekeeper extends Enemy {
    static hasMany = [questions: String]

    @Override
    String challenge() { 'Stop! Who would cross the Bridge of Death must answer me these questions three' }

    static constraints = {
        questions maxSize: 3
    }
}
```

Each subclass `extends Enemy`, adds its own properties and constraints, and overrides `challenge()`. The base class constraints still apply: a `BlackKnight` with a blank name is invalid. `hasMany = [questions: String]` is a collection of simple values rather than of domain objects; GORM stores it in a join table.

### Step 3: See the table

GORM's default inheritance strategy is **table per hierarchy**: one table for the base class and every subclass, with a discriminator column named `class` that records which subclass each row is. Export the schema and look:

```bash
./gradlew schemaExport
grep -i "create table enemy" build/ddl.sql
```

```sql
create table enemy (id bigint generated by default as identity, version bigint not null,
    name varchar(255) not null, location varchar(255) not null, defeated boolean not null,
    quest_id bigint not null, class varchar(255) not null,
    limbs_remaining integer, knights_eaten integer, weakness varchar(255), primary key (id));
create table bridgekeeper_questions (bridgekeeper_id bigint not null, questions_string varchar(255));
```

The subclass columns are nullable, since a rabbit has no `limbs_remaining`, and the base columns are `not null`. One table means polymorphic queries need no joins, at the cost of sparse rows. If you would rather have a table per subclass, set `tablePerHierarchy false` in the base class's `static mapping` block; GORM then joins on the primary key at query time.

`schemaExport` is a Gradle task provided by the Grails Gradle plugin; `grails schema-export` runs the same thing.

### Step 4: Seed the opposition

Add a third method to `SeedData.groovy`:

```groovy
static List<Enemy> theOpposition(Quest quest) {
    [
        new BlackKnight(name: 'The Black Knight', location: 'A bridge in the forest', quest: quest),
        new RabbitOfCaerbannog(name: 'The Rabbit of Caerbannog', location: 'The Cave of Caerbannog',
                knightsEaten: 3, quest: quest),
        new Bridgekeeper(name: 'The Bridgekeeper', location: 'The Bridge of Death', quest: quest,
                questions: ['What is your name?', 'What is your quest?', 'What is your favourite colour?']),
        new BlackKnight(name: 'The Black Knight, later', location: 'The same bridge', limbsRemaining: 0,
                defeated: true, quest: quest),
    ].each { it.save(failOnError: true) }
}
```

and call it from `BootStrap` after `theCourt(quest)`.

### Step 5: Test the hierarchy

A unit test can mock several domain classes at once with the `DataTest` trait. Delete the three generated subclass specs and replace `src/test/groovy/com/kousenit/EnemySpec.groovy`:

```groovy
package com.kousenit

import grails.testing.gorm.DataTest
import spock.lang.Specification

class EnemySpec extends Specification implements DataTest {

    Quest quest = new Quest(name: 'Seek the grail')

    void setupSpec() {
        mockDomains Quest, Enemy, BlackKnight, RabbitOfCaerbannog, Bridgekeeper
    }

    void "subclasses inherit the base constraints"() {
        expect:
        !new BlackKnight(name: ' ', location: 'A bridge', quest: quest).validate()
        new BlackKnight(name: 'The Black Knight', location: 'A bridge', quest: quest).validate()
    }

    void "each enemy has its own challenge"() {
        expect:
        new BlackKnight().challenge() == 'None shall pass'
        new BlackKnight(limbsRemaining: 0).challenge() == "All right, we'll call it a draw"
        new RabbitOfCaerbannog().challenge() == 'Look at the bones!'
        new Bridgekeeper().challenge().startsWith('Stop!')
    }

    void "a flesh wound is a matter of degree"() {
        expect:
        new BlackKnight(limbsRemaining: 1).merelyAFleshWound
        !new BlackKnight(limbsRemaining: 0).merelyAFleshWound
        !new BlackKnight(limbsRemaining: 5).validate()
    }

    void "the Bridgekeeper asks at most three questions"() {
        when:
        Bridgekeeper keeper = new Bridgekeeper(name: 'The Bridgekeeper', location: 'The Bridge of Death', quest: quest,
                questions: ['name', 'quest', 'colour', 'airspeed velocity of an unladen swallow'])

        then:
        !keeper.validate()
        keeper.errors['questions'].code == 'maxSize.exceeded'
    }

    void "queries on the base class are polymorphic"() {
        given:
        quest.save(failOnError: true)
        SeedData.theOpposition(quest)

        expect:
        Enemy.count() == 4
        BlackKnight.count() == 2
        Enemy.findAllByDefeated(false)*.class*.simpleName.sort() == ['BlackKnight', 'Bridgekeeper', 'RabbitOfCaerbannog']
        Enemy.list().collect { it.challenge() }.every { it }
    }
}
```

The last test is the point of the lab: `Enemy.count()` sees every subclass, `BlackKnight.count()` sees only its own, and the objects `Enemy.list()` returns are the concrete subclasses, so calling `challenge()` on each dispatches to the right override.

Then an integration test against the real database, `src/integration-test/groovy/com/kousenit/EnemyQueriesSpec.groovy`:

```groovy
package com.kousenit

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Rollback
class EnemyQueriesSpec extends Specification {

    private Quest seed() {
        Quest quest = SeedData.seekTheGrail()
        SeedData.theOpposition(quest)
        quest
    }

    void "one table holds the whole hierarchy, and GORM restores the right class"() {
        seed()

        when:
        List<Enemy> enemies = Enemy.list(sort: 'name')

        then:
        enemies*.class*.simpleName == ['BlackKnight', 'BlackKnight', 'Bridgekeeper', 'RabbitOfCaerbannog']
        enemies.find { it instanceof RabbitOfCaerbannog }.knightsEaten == 3
        enemies.find { it instanceof Bridgekeeper }.questions.size() == 3
    }

    void "finders on a subclass only see that subclass"() {
        seed()

        expect:
        BlackKnight.findAllByDefeated(false)*.name == ['The Black Knight']
        RabbitOfCaerbannog.findByKnightsEatenGreaterThan(0).name == 'The Rabbit of Caerbannog'
        Enemy.countByDefeated(true) == 1
    }

    void "where queries can mix base and subclass properties"() {
        seed()

        when:
        def undefeatedBlackKnights = BlackKnight.where { defeated == false && limbsRemaining > 0 }

        then:
        undefeatedBlackKnights.count() == 1
    }

    void "an enemy belongs to its quest"() {
        Quest quest = seed()

        expect:
        Enemy.findAllByQuest(quest).size() == 4
        Enemy.list().every { it.quest == quest }
    }
}
```

Run both:

```bash
./gradlew test
./gradlew integrationTest --tests 'com.kousenit.EnemyQueriesSpec'
```

### Step 6: Show the opposition

The enemies exist in the database and nowhere a visitor can click. Lab 6's lesson applies: one annotated class. Create `grails-app/controllers/com/kousenit/EnemyController.groovy`:

```groovy
package com.kousenit

import grails.plugin.scaffolding.annotation.Scaffold

@Scaffold(Enemy)
class EnemyController {

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [enemyList: Enemy.list(params), enemyCount: Enemy.count()]
    }

    def show(Long id) {
        [enemy: Enemy.get(id)]
    }
}
```

Two actions are overridden, and the reason is worth knowing. `RestfulController` renders with `respond`, which names the model variable after the **runtime class** of what it is given. Hand it a list whose first element is a `BlackKnight` and the view receives `blackKnightList`; hand it the Rabbit and the show view receives `rabbitOfCaerbannog`. The scaffolded views expect `enemyList` and `enemy`, so they render nothing. Returning a map from the action names the variables yourself.

A hierarchy trips the list template too. `f:table` infers its columns from the first row, so it tries to read `limbsRemaining` on the Rabbit and the page errors. Take over the list view and name the columns every enemy has:

```bash
grails generate-views com.kousenit.Enemy
rm grails-app/views/enemy/create.gsp grails-app/views/enemy/edit.gsp grails-app/views/enemy/show.gsp
```

then in `grails-app/views/enemy/index.gsp`, give `f:table` a `properties` attribute:

```html
<f:table class="scaffold table table-striped table-sm" controller="${controllerName}" collection="${enemyList}"
         properties="['name', 'location', 'defeated', 'quest']"/>
```

Run the app. **Enemies** appears on the welcome page, the list shows all four with links, and each show page renders its subclass's own properties: limbs remaining for the Black Knight, knights eaten and weakness for the Rabbit. The create form offers only the base-class properties, since the scaffolding cannot know which subclass you mean; creating subclasses through the web is a job for a custom form or the JSON API.

A one-line unit test proves the transformation, as in Lab 6:

```groovy
class EnemyControllerSpec extends Specification implements ControllerUnitTest<EnemyController> {
    void "the scaffolded controller serves the base class"() {
        expect:
        controller instanceof RestfulController
        controller.resource == Enemy
    }
}
```

### Key Learning Points

- `extends` is all GORM needs. The default mapping is one table per hierarchy with a `class` discriminator column; `tablePerHierarchy false` switches to joined tables.
- Base-class constraints apply to subclasses; subclasses add their own.
- Queries on the base class are polymorphic and return concrete subclass instances. Queries on a subclass are scoped to it, including its own properties.
- `hasMany = [questions: String]` stores a collection of simple values in a join table.
- `DataTest` with `mockDomains` unit-tests several domain classes together.
- `respond` names model variables after the runtime class. With a hierarchy, name them yourself. `f:table` infers columns from the first row; give it `properties`.

### Checkpoint

```bash
git checkout step10-enemies
```

## Lab 11: Optional: A JSON API for the Quest

Lab 6 showed that `RestfulController` answers JSON requests by default, serializing the whole domain object. That is fine for a quick look and wrong for an API: you rarely want every property, and you often want computed ones. **JSON views** give you a Groovy DSL for shaping the response, and the **HttpClientSupport** trait, new in Grails 7.1, gives you a fluent way to test the result over real HTTP. This lab builds an endpoint an AI client could call to ask how the quest is going.

### Step 1: Dependencies

Add JSON views to the application and the HTTP test support to the integration tests, in `build.gradle`:

```groovy
implementation "org.apache.grails:grails-views-gson"
```

```groovy
integrationTestImplementation "org.apache.grails:grails-testing-support-http-client"
integrationTestImplementation "org.apache.grails:grails-testing-support-dbcleanup-h2"
```

The third line is for `@DatabaseCleanup`, described in Step 5.

### Step 2: A REST controller and a URL mapping

Create `grails-app/controllers/com/kousenit/QuestApiController.groovy`. It is a `RestfulController`, like the scaffolded ones, restricted to JSON:

```groovy
package com.kousenit

import grails.rest.RestfulController

class QuestApiController extends RestfulController<Quest> {

    static responseFormats = ['json']

    QuestApiController() {
        super(Quest)
    }
}
```

Map it to a path in `grails-app/controllers/com/kousenit/UrlMappings.groovy`, above the `"/"` mapping:

```groovy
"/api/quests"(resources: "questApi")
```

`resources:` creates the full RESTful set of mappings for the controller: `GET /api/quests` to `index`, `GET /api/quests/1` to `show`, `POST /api/quests` to `save`, `PUT` and `PATCH` to `update`, `DELETE` to `delete`. Run `grails url-mappings-report` to see them.

### Step 3: JSON views

JSON views live in `grails-app/views/<controller>/` with the extension `.gson`. Create three files in `grails-app/views/questApi/`.

The template, `_quest.gson`, decides what one quest looks like:

```groovy
import com.kousenit.Quest

model {
    Quest quest
}

// A freshly created quest has null collections, hence the safe navigation.
json {
    id quest.id
    name quest.name
    knights quest.knights?.collect { it.toString() }?.sort() ?: []
    tasks quest.tasks?.sort { it.priority }?.collect { task ->
        [name: task.name, priority: task.priority, completed: task.completed]
    } ?: []
    remaining quest.tasks?.count { !it.completed } ?: 0
}
```

`index.gson` wraps the list the controller's `index` action responds with:

```groovy
import com.kousenit.Quest

model {
    List<Quest> questList
    Long questCount
}

json {
    count questCount
    quests g.render(template: 'quest', collection: questList, var: 'quest')
}
```

and `show.gson` renders a single one:

```groovy
import com.kousenit.Quest

model {
    Quest quest
}

json g.render(template: 'quest', model: [quest: quest])
```

The `model` block declares the variables the view expects, with types, and the views are compiled, so a typo in a property name is a compile error rather than a surprise in production. `g.render(template:)` reuses the template for one object or a collection. The property names `questList` and `questCount` are what `RestfulController.index` puts in the model.

Start the app and try it:

```bash
curl http://localhost:8080/api/quests
curl http://localhost:8080/api/quests/1
curl -X POST -H "Content-Type: application/json" -d '{"name":"Find a shrubbery"}' http://localhost:8080/api/quests
```

### Step 4: Test it over HTTP

Create `src/integration-test/groovy/com/kousenit/QuestApiSpec.groovy`:

```groovy
package com.kousenit

import grails.testing.mixin.integration.Integration
import org.apache.grails.testing.cleanup.core.DatabaseCleanup
import org.apache.grails.testing.http.client.HttpClientSupport
import spock.lang.Specification

@Integration
@DatabaseCleanup
class QuestApiSpec extends Specification implements HttpClientSupport {

    def setup() {
        Quest.withNewTransaction {
            SeedData.theCourt(SeedData.seekTheGrail())
        }
    }

    void "the quest list is JSON shaped by the view"() {
        when:
        def response = http('/api/quests')

        then:
        response.assertStatus(200)
                .assertHeadersIgnoreCase('content-type': 'application/json;charset=UTF-8')
        with(response.json()) {
            count == 1
            quests[0].name == 'Seek the grail'
            quests[0].knights.size() == 5
            quests[0].tasks.size() == 9
            quests[0].remaining == 6
            quests[0].tasks*.priority == quests[0].tasks*.priority.sort()
        }
    }

    void "a single quest can be fetched by id"() {
        given:
        Long id = Quest.withNewTransaction { Quest.findByName('Seek the grail').id }

        when:
        def response = http("/api/quests/$id")

        then:
        response.assertJsonContains([name: 'Seek the grail', remaining: 6])
    }

    void "posting JSON creates a quest"() {
        when:
        def response = httpPostJson('/api/quests', [name: 'Find a shrubbery'])

        then:
        response.assertStatus(201)
        response.assertJsonContains([name: 'Find a shrubbery', knights: [], tasks: [], remaining: 0])
        Quest.withNewTransaction { Quest.countByName('Find a shrubbery') } == 1
    }

    void "a blank name is rejected with 422"() {
        when:
        def response = httpPostJson('/api/quests', [name: ' '])

        then:
        response.assertStatus(422)
        response.assertContains('Quests must have a name')
    }

    void "unknown quests are 404"() {
        expect:
        http('/api/quests/9999').assertStatus(404)
    }
}
```

`HttpClientSupport` knows the port the `@Integration` test started the app on, so paths are relative. `http()` performs a GET, `httpPostJson()` turns a map into a JSON body, and the response wrapper's `assert...` methods chain. `response.json()` parses the body so you can use Spock's `with` on it.

Run it:

```bash
./gradlew integrationTest --tests 'com.kousenit.QuestApiSpec'
```

### Step 5: Cleaning up committed data

This spec makes real HTTP requests to a running server, so `@Rollback` cannot help: the request runs in its own transaction and commits. Earlier Grails versions left you to delete the data by hand in `cleanup()`. Grails 7 added `@DatabaseCleanup`, which truncates every table after each test method. It needs the cleanup module for your database, `grails-testing-support-dbcleanup-h2` here, and nothing else. The functional Geb test from Lab 9 uses the same annotation.

### Key Learning Points

- `RestfulController` plus a `resources:` URL mapping is a complete REST endpoint; `responseFormats` restricts it to JSON.
- JSON views are compiled Groovy with a typed `model` block and reusable templates.
- `HttpClientSupport` (7.1+) tests endpoints over real HTTP with fluent assertions and parsed JSON.
- `@DatabaseCleanup` replaces hand-written cleanup for tests that commit data.

### Checkpoint

```bash
git checkout step11-json
```
