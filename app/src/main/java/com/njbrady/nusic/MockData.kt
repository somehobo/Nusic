package com.njbrady.nusic

import com.njbrady.nusic.home.data.Song

class MockData {
    companion object {
        val testSongUrl = ""
        val testImageUrl = ""

        val userData = UserData(
            username = "Sir deez nuts",
            id = 1
        )

        val song1 = Song(
            songName = "So you'll aim towards the sky",
            artistName = "Grandaddy",
            artistId = 1,
            start = 1,
            end = 14,
            songUrl = testSongUrl,
            imageUrl = testImageUrl,
            songId = 1
        )

        val song2 = Song(
            songName = "Tell All Your Friend",
            artistName = "Okey Dokey",
            artistId = 1,
            start = 9,
            end = 30,
            songUrl = testSongUrl,
            imageUrl = testImageUrl,
            songId = 2
        )

        val song3 = Song(
            songName = "City Blues",
            artistName = "The Walters",
            artistId = 1,
            start = 3,
            end = 14,
            songUrl = testSongUrl,
            imageUrl = testImageUrl,
            songId = 3
        )

        val song4 = Song(
            songName = "Plastic Beach",
            artistName = "Gorillaz",
            artistId = 1,
            start = 30,
            end = 60,
            songUrl = testSongUrl,
            imageUrl = testImageUrl,
            songId = 4
        )

        val songList = listOf(
            song1,
            song2,
            song3,
            song4
        )
    }
}