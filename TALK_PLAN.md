# The Quest for the Holy Grails: A Grails 7 Tutorial

**Conference:** Community Over Code (Apache), Glasgow
**Date/Time:** 12 October 2026, 14:20–15:00 GMT
**Track:** Groovy — Room: Tweed
**Speaker:** Ken Kousen

> Note: "The Grooviest AI Client" runs in the same room at 12:00 the same day.
> A brief callback (e.g. the methodMissing AI client showing up as a Grails
> service) would make the two talks feel designed as a pair.

## Abstract (as submitted)

A hands-on tour of Apache Grails 7 — the first release as an ASF Top-Level
Project — through a Monty Python quest domain. Knights, castles, quests, and
tasks guide us from domain modeling through constraints, services, testing,
and what's new in Grails 7.

## Background

Also an "aspirational" talk. The abstract as written is a full-day workshop;
in 40 minutes it needs to be cut to three beats. Skip the controller/view layer
entirely, or wave at scaffolding once and move on.

## Grails 7 facts (verified 5 Oct 2026; superseded by the 11 Oct note at the end)

- Grails 7.0.0 shipped 28 October 2025 — the first stable release after
  graduating to an ASF Top-Level Project.
- **Current release: 7.2.4** (about two weeks old). Also maintained:
  7.1.7 and 7.0.17. This project targets 7.2.4.
- **Grails 8.0.0 was tagged 4 Oct 2026** but is still marked pre-release on
  GitHub (ASF vote presumably in progress); 8.0.0-RC2 is what Forge serves as
  NEXT. It may be announced the week of the conference — have a one-slide
  "and here comes 8" ready. The Grails site already advertises a JavaMUG talk
  "An Introduction to Apache Grails 8" on 11 Nov 2026.
- 7.2.4 stack (from Forge BOM): Java 17/21/25, Groovy 4.0.33, Spring Boot
  3.5.16, Hibernate 5.6.15 (GORM default), H2 2.4, Geb 8.0.1, Spock 2.3,
  Gradle 8.14.5.
- 8.0.0-RC2 stack (per its docs): **Java 21 baseline**, Gradle 9.8.0, Groovy 5.1.3,
  Spring Boot 4.1.1 / Spring Framework 7.0.9 (Jackson 3, Tomcat 11, Servlet
  6.1), Spock 2.4-groovy-5.0, Hibernate 7 via Forge, jQuery 4, Undertow option.
  Grails 8's own build uses the CycloneDX Gradle plugin, which is probably
  where the "SBOM" rumour came from; it is not an application feature.
- Grails 7 features (verified against the 7.2.4 guide): containerized browser
  testing with Testcontainers (Geb), @Scaffold, HttpClientSupport (7.1),
  @DatabaseCleanup, custom test phases (7.1), audit annotations (7.1), external
  configuration integration, Micronaut removed (opt-in plugin). Reproducible
  builds are an opt-in via SOURCE_DATE_EPOCH. **No SBOM feature is documented;
  dropped.** `grails console` and `schema-export` still exist in 7.2.4.
- ASF transition changes: mono repo, reworked CLIs, modernized Gradle plugins
  and tasks, and **new Maven coordinates for all artifacts** (e.g.
  `org.apache.grails:grails-core`, `org.apache.grails.profiles:web`). This
  matters to anyone upgrading from Grails 6 — likely half the room.
- Companion plugin major releases: Spring Security 7.0.0, Quartz 4.0.0,
  Redis 5.0.0.
- Forge moved: start.grails.org now redirects to grails.apache.org/start.
  API hosts are latest.grails.org (RELEASE) and next.grails.org (NEXT).
- Gotcha hit while setting up: `org.gradle.configuration-cache=true` in
  `~/.gradle/gradle.properties` breaks the 7.2.4 `buildProperties` task.
  A project-level gradle.properties cannot override it (user-home wins), so
  build.gradle marks that task `notCompatibleWithConfigurationCache`.
- Fun callback for the AI talk: the Grails 8 release notes mention a
  "Grails 8 Upgrade skill" and list @claude among contributors.

## Project setup (done 5 Oct 2026)

- Generated with Forge: web profile, `com.kousenit.holygrails`, JDK 21 (Temurin 21.0.8 via .sdkmanrc),
  Hibernate, Spock, Testcontainers feature. Committed untouched.
- `.sdkmanrc` pins java 21.0.8-tem and grails 7.2.4.
- **JDK decision (6 Oct):** 21, not 25. Grails 7.2.4 requires Gradle 8.14.x
  (its plugins break on Gradle 9: `groovyOptions.configurationScriptFile is
  final`), and Gradle 8.14 officially supports Java 24 at most. IntelliJ
  enforces that table and refuses to sync on JDK 25. Gradle 9.8 + Java 25 is a
  Grails 8 item for the "what's coming" slide.
- `./gradlew test` passes on the empty app.

## Decisions (6 Oct 2026)

- **Two deliverables:** a staged tutorial repo with `labs.md` and a git tag per
  lab (Ken's training-class format), plus a 40-minute demo that walks a subset
  of the labs live. Main always holds the finished app.
- **Baseline code:** github.com/kousen/holygrails500 (Grails 5.0.0) is the
  starting domain model; it already uses `LocalDate` in `Task`. The Grails 3.3
  labs (archived kousenit.com site, `grails-3.3/labs/*.html`) supply the lab
  progression and the Monty Python seed data.
- **Geocoding:** Open-Meteo geocoding API (no key, JSON). BootStrap ships
  hard-coded coordinates; the service runs only for castles added via the UI,
  so startup never touches the network. Map: Leaflet + OpenStreetMap tiles.
- **Finders/criteria** move from the Grails console (still available, but
  not a tutorial medium) into a Spock integration test.
- **Expansion for the "AI builds the rest" argument:** `Enemy` hierarchy for
  GORM inheritance (BlackKnight, RabbitOfCaerbannog, Bridgekeeper) and a JSON
  views endpoint tested with `HttpClientSupport` (7.1+).
- **Exercises:** the geocoder method (and the Galahad validator) are presented
  as exercises with the code supplied in the lab, Ken's usual format.
- **Headline Grails 7 slide:** `@Scaffold` before/after using the 103-line
  generated `CastleController` from holygrails500.
- **Security follow-up:** holygrails500's `GeocoderService` has a Google Maps
  API key committed in a public repo. Revoke it.
- **Conference context (from the desktop conversation):** James's Grails 8
  talk is at 17:00 the same day; Mattias covers Geb/Testcontainers. One line
  acknowledging each and hand off.

## Stages (all built and tagged, 6 Oct 2026; `labs.md` has the walkthrough)

| Tag | Lab | In the demo? |
|---|---|---|
| `step0-starter` | Creating the project | Finished state, 2 min |
| `step1-quest` | Quest, scaffolding, blank constraint, message | Live |
| `step2-task` | Task, belongsTo, range, date validator, duration | Live (validator) |
| `step3-testing` | Spock specs with @Unroll | Shown |
| `step4-queries` | Seed data; finders, criteria, where as integration test | Live (two finders) |
| `step5-model` | Knight, Castle, Galahad rule | Shown with seed data |
| `step6-scaffold` | @Scaffold on CastleController, before/after from 5.0 | Live, headline |
| `step7-geocoder` | Open-Meteo GeocoderService, mocked unit test, integration test | Live |
| `step8-map` | Leaflet castle map | Shown, payoff |
| `step9-grails7` | What's new, upgrade notes, ContainerGebSpec | Slides |
| `step10-enemies` | Enemy hierarchy (GORM inheritance) | If time |
| `step11-json` | JSON views endpoint + HttpClientSupport | Optional |

## Slides

- `slides.md` (Slidev, seriph theme, 42 slides) built 6 Oct 2026; `npm run export`
  produces the PDF. Speaker notes mark the LIVE moments.

## Practical notes

- Live-code only what the table marks Live; everything else is `git checkout`.
- Hotel wifi is the enemy: nothing in the demo path needs the network.
- Monty Python references should do structural work (names, constraints,
  finders), not add material.
- **Geb on Apple silicon, resolved (6 Oct):** default Chrome image crashes under
  Docker VMM (QEMU emulation). With Apple Virtualization framework + Rosetta the
  amd64 image works unchanged. Repo keeps the Firefox GebConfig; Lab 1 documents
  both. Ken's Docker is now on Apple VF + Rosetta.
- **holygrails8 branch (6 Oct):** app on 8.0.0-RC2 / Gradle 9.8 / JDK 25, Hibernate
  5.6 kept; 76 unit (1 @PendingFeature for RC2 DomainUnitTest bug #16466) + 41
  integration green. Findings: Grails 8 makes unconstrained properties nullable
  by default (`grails.gorm.default.nullable: false` restores); RestfulController
  index count is Long; Forge 8 starter swaps layout/assets/i18n/config. Demo
  moment: `git checkout holygrails8`, same tests, JDK 25.

## Grails 8.0.0 released (11 Oct 2026, the day before the talk)

- grails.apache.org lists 8.0.0 as current; Forge `latest.grails.org` now
  serves 8.0.0 by default (Groovy 5.1.3, Spring Boot 4.1.1, Spring 7.0.9,
  Gradle 9.8.0, Spock 2.4-groovy-5.0, Hibernate 5.6.15 still the default,
  Tomcat 11). So anyone running the Lab 0 curl today gets a Grails 8 app.
- **main is now Grails 8.0.0, JDK 25 (Temurin 25.0.4), Gradle 9.8.** The
  `holygrails8` RC2 branch was merged, RC2 bumped to 8.0.0, the 8.0.0 Forge
  starter's welcome page and i18n bundles adopted (the filter table changed
  between RC2 and GA), `htmlcodec: xml` dropped (now the default), the JSON
  view declares `Long questCount`, and the `@PendingFeature` on TaskSpec is
  gone because 8.0.0 honours `grails.gorm.default.nullable` in DomainUnitTest
  (#16466). 76 unit + 41 integration green, with the Gradle configuration
  cache on; `console` and `schemaExport` tasks still exist; the welcome page
  sends the new security headers.
- **Tags stay on 7.2.4.** Re-cutting twelve tags on an 8 base was judged too
  risky the day before the talk; labs.md and README say the tags carry 7.2.4
  build files and identical application code. Candidate follow-up.
- Docs: labs.md is now "Grails 8 Labs" (JDK 25, Grails 8.0.0, Lab 9 rewritten
  as "What's New in Grails 8 (and 7)" with the 7→8 checklist and the port);
  slides subtitle "A Grails 8 Tutorial", "Grails 8 at a glance" table, beat
  three opens with nullable-by-default, the four-change port, "also new in 8",
  the 7→8 checklist, then the existing 7 slides for people on 5/6. The
  "Grails 8 is next" slide is gone. Programme still says "Grails 7 Tutorial":
  opening line.
- Still true from the 7 era: groovy-datetime needed, Apple silicon Geb fix,
  setup()/@Rollback ordering, where-closure-in-expect quirk.
