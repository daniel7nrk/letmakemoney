package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.auth.SupabaseAuthManager
import com.example.data.AppDatabase
import com.example.data.TransactionEntity
import com.example.sync.NetworkMonitor
import com.example.sync.SupabaseSyncManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SupabaseIntegrationTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testSupabaseAuthManagerInitialization() {
        val auth = SupabaseAuthManager.getInstance(context)
        assertNotNull(auth)
        // Ensure clean sign out state
        auth.signOut()
        assertFalse(auth.isAuthenticated.value)
        assertNull(auth.accessToken)
        assertNull(auth.userId)
    }

    @Test
    fun testSupabaseAuthValidation() = runBlocking {
        val auth = SupabaseAuthManager.getInstance(context)
        auth.signOut()

        // 1. Empty email / password should fail validation
        val emptySignIn = auth.signIn("", "")
        assertFalse(emptySignIn)
        assertNotNull(auth.authError.value)

        // 2. Short password in signUp (< 6 chars) should fail validation
        val shortPassSignUp = auth.signUp("user@example.com", "123")
        assertFalse(shortPassSignUp)
        assertTrue(auth.authError.value?.contains("at least 6 characters") == true)
    }

    @Test
    fun testUnsyncedTransactionsDao() = runBlocking {
        val dao = database.transactionDao()

        val tx1 = TransactionEntity(
            id = 1,
            amount = 5000.0,
            currency = "RWF",
            dateEpochMillis = System.currentTimeMillis(),
            description = "Coffee",
            merchant = "Simba Cafe",
            category = "Food & Dining",
            source = "SMS",
            status = "CONFIRMED",
            isSynced = false
        )

        val tx2 = TransactionEntity(
            id = 2,
            amount = 12000.0,
            currency = "RWF",
            dateEpochMillis = System.currentTimeMillis(),
            description = "Groceries",
            merchant = "Simba Supermarket",
            category = "Shopping",
            source = "MANUAL",
            status = "CONFIRMED",
            isSynced = true
        )

        dao.insertTransaction(tx1)
        dao.insertTransaction(tx2)

        val unsynced = dao.getUnsyncedTransactions()
        assertEquals(1, unsynced.size)
        assertEquals(1L, unsynced[0].id)
        assertFalse(unsynced[0].isSynced)

        // Mark as synced
        dao.markTransactionsAsSynced(listOf(1L))
        val afterSync = dao.getUnsyncedTransactions()
        assertTrue(afterSync.isEmpty())
    }

    @Test
    fun testSupabaseSyncManagerOfflineSafe() {
        // Safe to call offline or unauthenticated; must never crash
        SupabaseSyncManager.syncAllPendingData(context)
        SupabaseSyncManager.syncIfOnline(context)
    }

    @Test
    fun testNetworkMonitorInitialization() {
        val monitor = NetworkMonitor.getInstance(context)
        assertNotNull(monitor)
    }
}
