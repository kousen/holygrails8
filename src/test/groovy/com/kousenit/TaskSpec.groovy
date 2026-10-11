package com.kousenit

import grails.testing.gorm.DomainUnitTest
import spock.lang.PendingFeature
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

    // Grails 8.0.0-RC2's DomainUnitTest ignores grails.gorm.default.nullable, so a null
    // quest validates. Fixed for 8.0.0 by apache/grails-core#16466; Spock will fail this
    // spec the moment the feature starts passing, which is the signal to remove the annotation.
    @PendingFeature(reason = 'RC2 unit tests do not honor grails.gorm.default.nullable')
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
