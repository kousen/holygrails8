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

    void "Tim the Enchanter has no title, and does not print one"() {
        given:
        Knight tim = new Knight(title: null, name: 'Tim the Enchanter', favouriteColour: 'Fire')

        expect:
        tim.validate()
        tim.toString() == 'Tim the Enchanter'
    }

    void "a blank title from a form means no title"() {
        expect:
        new Knight(title: '  ', name: 'Tim the Enchanter', favouriteColour: 'Fire').title == null
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
