# The Quest for the Holy Grails

A complete [Apache Grails 7](https://grails.apache.org) application built in stages around a Monty Python quest domain: knights, castles, quests, tasks, and the enemies in the way. It accompanies the talk *The Quest for the Holy Grails: A Grails 7 Tutorial* at Community Over Code, Glasgow, October 2026.

**Start with [labs.md](labs.md).** It walks through every stage, from generating the project to a JSON API, and each stage ends with a git tag:

| Tag | Lab |
|---|---|
| `step0-starter` | Creating the project |
| `step1-quest` | The first domain class, scaffolding, constraints |
| `step2-task` | A related domain class, `belongsTo`, validators |
| `step3-testing` | Unit testing domain classes with Spock |
| `step4-queries` | Seed data; dynamic finders, criteria, where queries |
| `step5-model` | Knight and Castle; the Bridge of Death validator |
| `step6-scaffold` | `@Scaffold` replaces 535 lines of generated code |
| `step7-geocoder` | A service calling Open-Meteo, tested with a Spy |
| `step8-map` | A Leaflet map of the castles |
| `step9-grails7` | What's new in Grails 7; a containerized Geb test |
| `step10-enemies` | Inheritance in GORM |
| `step11-json` | JSON views and `HttpClientSupport` |

`main` is the finished application. To see a stage, `git checkout <tag>`.

## This branch: Grails 8.0.0-RC2

This is the `holygrails8` branch: the same application on **Grails 8.0.0-RC2**, Groovy 5.1, Spring Boot 4.1, Gradle 9.8 and JDK 25, with Hibernate 5.6 kept. `main` and the `step*` tags stay on Grails 7.2.4; this branch exists to show what the upgrade touches. Everything passes: 76 unit tests (one pending, see below) and 41 integration tests, browser and HTTP tests included.

What changed to get here, in order of how much it matters:

1. **Unconstrained properties are nullable by default in Grails 8.** Every earlier Grails applied an implicit `nullable: false`; 8 adopts JPA and Bean Validation semantics, so a `Task` with no name and no quest validated *and saved*, and `schemaExport` produced nullable columns. `grails.gorm.default.nullable: false` in `application.yml` restores the old rule application-wide. The alternative is `nullable: false` on every property that must be present.
2. `RestfulController.index` now puts a `Long` count in the model where 7 used an `Integer`; the compiled JSON view's `model` block rejected it. `index.gson` declares `Number questCount`.
3. The Forge 8 starter replaced the layout (SiteMesh 3 with a navbar and dark mode), the welcome assets, the i18n bundles and the config files; those were adopted wholesale and our three messages, URL mapping and config keys re-applied.
4. `build.gradle` follows the Forge 8 starter: `grails-sitemesh3` instead of `grails-layout`, explicit Spring Boot starters, `testcontainers-spock`, `compileJava.options.release = 21`, no `console` configuration. The Grails 7 configuration-cache workaround is gone; Grails 8 no longer needs it.
5. `DomainUnitTest` in RC2 does not honor the configured nullable default, so "a task needs a quest" in `TaskSpec` is `@PendingFeature` with a reference to the 8.0.0 fix.

What did not change: every domain class, controller, service, view and test other than the two lines above. The `groovy-datetime` dependency is still needed on Groovy 5.


Requires JDK 25 and Grails 8.0.0-RC2; `sdk env` selects both via [SDKMAN](https://sdkman.io). Docker is needed only for the browser tests.

```bash
grails run-app          # http://localhost:8080 (or ./grailsw run-app without installing Grails)
./gradlew test             # unit tests
./gradlew integrationTest  # integration, HTTP and browser tests
OFFLINE=1 ./gradlew integrationTest   # skip the live geocoder test
```

## Slides

The talk deck is `slides.md`, a [Slidev](https://sli.dev) presentation:

```bash
npm install
npm run dev      # present at http://localhost:3030
npm run export   # holygrails7-slides.pdf
```

The title photo of Doune Castle is by Bill Boaden, CC BY-SA 2.0, via geograph.org.uk and Wikimedia Commons.

## Licence

Apache License 2.0.
