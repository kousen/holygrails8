package com.kousenit

import java.time.LocalDate

/**
 * The quest for the Holy Grail, as a set of tasks. Used by BootStrap for the
 * running application and by the integration tests, so both see the same data.
 */
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

    /**
     * Where the film was shot: Doune Castle played Camelot (and Swamp Castle's
     * interior, Castle Anthrax, and the French castle); Castle Stalker was
     * Castle Aaargh. Coordinates are hard-coded so startup never needs the network.
     */
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
        // No title, no castle. There are some who call him... Tim.
        new Knight(title: null, name: 'Tim the Enchanter', favouriteColour: 'Fire').save(failOnError: true)
        [camelot, aaargh, swamp]
    }

    /** The opposition, one of each kind, so polymorphic queries have something to find. */
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
}
