package com.kousenit

class Knight {
    String title = 'Sir'
    String name
    String favouriteColour
    Quest quest
    Castle castle

    /** A blank title from a form means no title at all. There are some who call him... Tim. */
    void setTitle(String title) { this.title = title?.trim() ?: null }

    String toString() { [title, name].findAll().join(' ') }

    static constraints = {
        title nullable: true, inList: ['Sir', 'Lord', 'Lady', 'King', 'Queen']
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
