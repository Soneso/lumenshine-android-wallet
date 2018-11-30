package com.soneso.lumenshine.presentation.auth.setup

class MnemonicQuizHelper(mnemonic: String) {

    private val words = ("fake $mnemonic").split(" ")
    val wordsToGuess = words.shuffled().subList(0, 4)

    fun checkPositions(pos1: CharSequence, pos2: CharSequence, pos3: CharSequence, pos4: CharSequence): Boolean {
        return wordsToGuess[0] == words[pos1.toNumber()]
                && wordsToGuess[1] == words[pos2.toNumber()]
                && wordsToGuess[2] == words[pos3.toNumber()]
                && wordsToGuess[3] == words[pos4.toNumber()]
    }

    private fun CharSequence.toNumber(): Int {
        var nr = toString().toIntOrNull() ?: 0
        if (nr < 0 || nr > 24) {
            nr = 0
        }
        return nr
    }
}