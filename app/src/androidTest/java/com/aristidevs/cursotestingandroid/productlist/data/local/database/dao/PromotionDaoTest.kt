package com.aristidevs.cursotestingandroid.productlist.data.local.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aristidevs.cursotestingandroid.core.data.local.database.MiniMarketDatabase
import com.aristidevs.cursotestingandroid.productlist.data.local.database.dao.builder.promotionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PromotionDaoTest {

    private lateinit var database: MiniMarketDatabase
    private lateinit var promotionDao: PromotionDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            MiniMarketDatabase::class.java
        ).build()
        promotionDao = database.promotionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun should_get_all_promotions_in_getAllPromotions() = runTest {
        // GIVEN
        val promotions = listOf(
            promotionEntity { withId("1") },
            promotionEntity { withId("2") }
        )
        promotionDao.insertPromotions(promotions)

        // WHEN
        val result = promotionDao.getAllPromotions().first()

        // THEN
        assertEquals(2, result.size)
        assertEquals("1", result[0].id)
        assertEquals("2", result[1].id)
    }

    @Test
    fun should_clear_all_promotions_in_clearPromotions() = runTest {
        // GIVEN
        val promotions = listOf(promotionEntity { withId("1") })
        promotionDao.insertPromotions(promotions)

        // WHEN
        promotionDao.clearPromotions()
        val result = promotionDao.getAllPromotions().first()

        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun should_replace_all_promotions_in_replaceAll() = runTest {
        // GIVEN
        val initialPromotions = listOf(promotionEntity { withId("old") })
        promotionDao.insertPromotions(initialPromotions)
        val newPromotions = listOf(
            promotionEntity { withId("new-1") },
            promotionEntity { withId("new-2") }
        )

        // WHEN
        promotionDao.replaceAll(newPromotions)
        val result = promotionDao.getAllPromotions().first()

        // THEN
        assertEquals(2, result.size)
        assertEquals("new-1", result[0].id)
        assertEquals("new-2", result[1].id)
    }

    @Test
    fun should_replace_promotion_when_conflict_in_insertPromotions() = runTest {
        // GIVEN
        val promoId = "1"
        val initialPromo = promotionEntity { withId(promoId); withPercent(10) }
        promotionDao.insertPromotions(listOf(initialPromo))
        val updatedPromo = promotionEntity { withId(promoId); withPercent(20) }

        // WHEN
        promotionDao.insertPromotions(listOf(updatedPromo))
        val result = promotionDao.getAllPromotions().first()

        // THEN
        assertEquals(1, result.size)
        assertEquals(20, result[0].percent)
    }
}
