package com.example

import com.example.data.CategoryEntity
import com.example.parser.SmsParser
import org.junit.Assert.*
import org.junit.Test

class SmsParserExcelTest {

    private val categories = listOf(
        CategoryEntity(id = 1, name = "Food & Dining", iconName = "restaurant", tag = "Essential", colorHex = "#FFDAD6", keywords = "starbucks,coffee,food,dining,restaurant,supermarket,simba,grocery"),
        CategoryEntity(id = 2, name = "Rent & Utilities", iconName = "home", tag = "Fixed", colorHex = "#D7E2FF", keywords = "apartment,rent,utilities,housing,electricity,water,skyline,wasac"),
        CategoryEntity(id = 3, name = "Salary", iconName = "payments", tag = "Income", colorHex = "#FFDDB4", keywords = "salary,income,payroll,deposit"),
        CategoryEntity(id = 4, name = "Transport", iconName = "directions_car", tag = "Commute", colorHex = "#DAE2FF", keywords = "uber,yego,moto,cab,metro,fuel,shell,bus"),
        CategoryEntity(id = 5, name = "Entertainment", iconName = "movie", tag = "Lifestyle", colorHex = "#FFB955", keywords = "netflix,cinema,movie,entertainment,spotify"),
        CategoryEntity(id = 6, name = "Shopping", iconName = "shopping_bag", tag = "Lifestyle", colorHex = "#E1E3E4", keywords = "amazon,mall,shopping,store"),
        CategoryEntity(id = 7, name = "Health", iconName = "medical_services", tag = "Essential", colorHex = "#FFDAD6", keywords = "pharmacy,hospital,doctor,clinic"),
        CategoryEntity(id = 8, name = "Bills", iconName = "payments", tag = "Fixed", colorHex = "#EDEEEF", keywords = "bill,tax,fee,airtime"),
        CategoryEntity(id = 9, name = "Transaction Fees", iconName = "payments", tag = "Fixed", colorHex = "#FEF3C7", keywords = "fee,charges,tariff")
    )

    @Test
    fun testRow1_Payment_Airtime_Fee0() {
        val sms = "*162*TxId:30077065334*S*Your payment of 200 RWF to Airtime with token and ET Id: 30077065334 was completed at 2026-08-24 09:23:59. Fee 0 RWF. Balance: 82796 RWF . Message: - -. *EN#`"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(200.0, res!!.amount, 0.01)
        assertEquals(0.0, res.fee, 0.01)
        assertEquals("Airtime", res.merchant)
        assertTrue(res.shouldRecord)
        assertNotNull(res.timestampEpochMillis)
    }

    @Test
    fun testRow2_Payment_WASAC_Fee300() {
        val sms = "*162*TxId:30919363241*S*Your payment of 28108 RWF to WASAC with token  and ET Id: 4353876 was completed at 2026-09-30 20:28:09. Fee 300 RWF. Balance: 95278 RWF . Message: - Posted successfully. *EN#"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(28108.0, res!!.amount, 0.01)
        assertEquals(300.0, res.fee, 0.01)
        assertEquals("WASAC", res.merchant)
        assertTrue(res.shouldRecord)
        assertNotNull(res.timestampEpochMillis)
    }

    @Test
    fun testRow3_Received_ShouldNotRecord() {
        val sms = "You have received 30000 RWF from Francoise CIZIGIRE M NTANE (250783336690) on your mobile money account at 2025-04-13 11:09:20. Message from sender: . Your new balance:75367 RWF. Financial Transaction Id: 20101936432."
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(30000.0, res!!.amount, 0.01)
        assertEquals(0.0, res.fee, 0.01)
        assertFalse(res.shouldRecord) // Record: No Leave it!
    }

    @Test
    fun testRow4_Payment_eKash_Fee20() {
        val sms = "*162*TxId:20038126429*S*Your payment of 300 RWF to eKash with token  has been completed at 2025-04-09 11:24:00. Fee was 20 RWF. Your new balance: 757 RWF . Message: - ACSC. *EN#"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(300.0, res!!.amount, 0.01)
        assertEquals(20.0, res.fee, 0.01)
        assertEquals("eKash", res.merchant)
        assertTrue(res.shouldRecord)
    }

    @Test
    fun testRow5_Received_Mike_ShouldNotRecord() {
        val sms = "You have received 100 RWF from Mike s Dailly ltd Mike s Dailly ltd (*********336) at 2026-10-08 18:31:23. Balance:87302 RWF. FT Id: 31098712219"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(100.0, res!!.amount, 0.01)
        assertEquals(0.0, res.fee, 0.01)
        assertFalse(res.shouldRecord)
    }

    @Test
    fun testRow6_Received_Deborah_ShouldNotRecord() {
        val sms = "You have received 30000 RWF from Deborah NGARUKA (*********726) at 2026-10-10 10:28:31 . Balance:87832 RWF. FT Id: 31134490104."
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(30000.0, res!!.amount, 0.01)
        assertEquals(0.0, res.fee, 0.01)
        assertFalse(res.shouldRecord)
    }

    @Test
    fun testRow7_Payment_Jean_Fee0() {
        val sms = "TxId:31136195699*S*Your payment of 10,050 RWF to Jean 042392 was completed at 2026-10-10 11:51:45.  Balance: 54,932 RWF. Fee 0 RWF.*EN#"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(10050.0, res!!.amount, 0.01)
        assertEquals(0.0, res.fee, 0.01)
        assertEquals("Jean 042392", res.merchant)
        assertTrue(res.shouldRecord)
    }

    @Test
    fun testRow8_Transferred_Ayubu_Fee20() {
        val sms = "*165*S*665 RWF transferred to Ayubu KABERUKA (250786542519) at 2026-10-10 12:46:41 .Fee: 20RWF.Balance: 51447RWF.Dial *182*1*3# and send money abroad *EN#"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(665.0, res!!.amount, 0.01)
        assertEquals(20.0, res.fee, 0.01)
        assertEquals("Ayubu KABERUKA", res.merchant)
        assertTrue(res.shouldRecord)
    }

    @Test
    fun testRow9_Transferred_Alphonsine_Fee100() {
        val sms = "*165*S*3000 RWF transferred to Alphonsine MUSABYIMANA (250784685310) at 2026-10-10 12:52:37 .Fee: 100RWF.Balance: 48347RWF.Dial *182*1*3# and send money abroad *EN#"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(3000.0, res!!.amount, 0.01)
        assertEquals(100.0, res.fee, 0.01)
        assertEquals("Alphonsine MUSABYIMANA", res.merchant)
        assertTrue(res.shouldRecord)
    }

    @Test
    fun testRow10_Transferred_Dismas_Fee100() {
        val sms = "*165*S*2000 RWF transferred to Dismas MANIRAFASHA (250780312445) at 2026-10-10 12:57:00 .Fee: 100RWF.Balance: 46247RWF.Dial *182*1*3# and send money abroad *EN#"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(2000.0, res!!.amount, 0.01)
        assertEquals(100.0, res.fee, 0.01)
        assertEquals("Dismas MANIRAFASHA", res.merchant)
        assertTrue(res.shouldRecord)
    }

    @Test
    fun testRow11_Transferred_MarieRose_Fee100() {
        val sms = "*165*S*2000 RWF transferred to Marie Rose NYINAWABASINGA (250788601512) at 2026-10-10 12:58:48 .Fee: 100RWF.Balance: 44147RWF.Dial *182*1*3# and send money abroad *EN#"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(2000.0, res!!.amount, 0.01)
        assertEquals(100.0, res.fee, 0.01)
        assertEquals("Marie Rose NYINAWABASINGA", res.merchant)
        assertTrue(res.shouldRecord)
    }

    @Test
    fun testRow12_Transferred_Vestine_Fee20() {
        val sms = "*165*S*500 RWF transferred to Vestine TWIZEYIMANA (250791355614) at 2026-10-10 13:15:41 .Fee: 20RWF.Balance: 43627RWF.Dial *182*1*3# and send money abroad *EN#"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(500.0, res!!.amount, 0.01)
        assertEquals(20.0, res.fee, 0.01)
        assertEquals("Vestine TWIZEYIMANA", res.merchant)
        assertTrue(res.shouldRecord)
    }

    @Test
    fun testRow13_Transferred_JeanBosco_Fee100() {
        val sms = "*165*S*1500 RWF transferred to Jean Bosco HABIMANA (250789331603) at 2026-10-10 13:38:05 .Fee: 100RWF.Balance: 42027RWF.Dial *182*1*3# and send money abroad *EN#"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(1500.0, res!!.amount, 0.01)
        assertEquals(100.0, res.fee, 0.01)
        assertEquals("Jean Bosco HABIMANA", res.merchant)
        assertTrue(res.shouldRecord)
    }

    @Test
    fun testRow14_Received_Floride_ShouldNotRecord() {
        val sms = "You have received 500 RWF from FLORIDE NYIRANDAYISABYE FLORIDE NYIRANDAYISABYE (*********157) at 2026-10-10 15:11:23. Balance:41507 RWF. FT Id: 31140289095"
        val res = SmsParser.parseSms("MTN", sms, categories, "RWF")
        assertNotNull(res)
        assertEquals(500.0, res!!.amount, 0.01)
        assertEquals(0.0, res.fee, 0.01)
        assertFalse(res.shouldRecord)
    }
}
