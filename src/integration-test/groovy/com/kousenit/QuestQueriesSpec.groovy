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
        urgent.list(sort: 'name')*.name == ['Answer the Bridgekeeper', 'Lobbeth thou thy Holy Hand Grenade of Antioch']
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
