package com.codingtest.iprange.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class RegionTest {

    /**
     * Test Spec: Validates that each known region string maps correctly to its corresponding Region enum.
     */
    @Test
    fun fromString_shouldReturnRegionEnumWhenNameMatches() {
        val resultUs = Region.fromString("us")
        assertEquals(Region.US, resultUs)

        val resultEu = Region.fromString("eu")
        assertEquals(Region.EU, resultEu)

        val resultAs = Region.fromString("as")
        assertEquals(Region.AS, resultAs)

        val resultMe = Region.fromString("me")
        assertEquals(Region.ME, resultMe)

        val resultNa = Region.fromString("na")
        assertEquals(Region.NA, resultNa)

        val resultSa = Region.fromString("sa")
        assertEquals(Region.SA, resultSa)

        val resultAf = Region.fromString("af")
        assertEquals(Region.AF, resultAf)

        val resultAus = Region.fromString("aus")
        assertEquals(Region.AUS, resultAus)

        val resultAll = Region.fromString("all")
        assertEquals(Region.ALL, resultAll)

        val resultGl = Region.fromString("gl")
        assertEquals(Region.GL, resultGl)

    }

    /**
     * Test Spec: Ensures that invalid or unknown region strings return null from fromString().
     */
    @Test
    fun fromString_shouldReturnNullWhenInputDoesNotMatchAnyRegion() {
        val result = Region.fromString("abc")
        assertNull(result)

        val empty = Region.fromString("")
        assertNull(empty)
    }

    /**
     * Test Spec: Verifies that each Region does not mistakenly match unrelated scope names.
     */
    @Test
    fun shouldNotMatchUnrelatedScopes() {
        assertFalse(Region.EU.scopes.any { "us-west1".contains(it) })
        assertFalse(Region.AUS.scopes.any { "asia-east1".contains(it) })
        assertFalse(Region.AF.scopes.any { "southamerica-east1".contains(it) })
    }

    /**
     * Test Spec: Confirms that each Region correctly matches at least one valid scope pattern it is responsible for.
     */
    @Test
    fun shouldMatchScopeCorrectlyForEachRegion() {
        assertTrue(Region.US.scopes.any { "us-west1".contains(it) })
        assertTrue(Region.EU.scopes.any { "europe-west1".contains(it) })
        assertTrue(Region.ME.scopes.any { "me-central1".contains(it) })
        assertTrue(Region.SA.scopes.any { "southamerica-east1".contains(it) })
        assertTrue(Region.GL.scopes.any { "global".contains(it) })
    }

}
