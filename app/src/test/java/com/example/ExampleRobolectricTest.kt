package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ChatHistoryEntity
import com.example.data.HomeBannerEntity
import com.example.data.RoomSettingsEntity
import com.example.data.UserAccountEntity
import com.example.data.ZadiraDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Zadira Live", appName)
  }

  @Test
  fun `roomDatabase persists userData roomSettings chatHistory and databaseBanners`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(
      context,
      ZadiraDatabase::class.java
    ).allowMainThreadQueries().build()
    val dao = db.appDao()

    dao.saveUserAccount(
      UserAccountEntity(
        uuid = "user_my_account",
        displayId = "10001",
        isSpecialId = true,
        email = "king@zadira.live",
        nickname = "الأمير محمد الملكي",
        bio = "مرحباً بكم في ديوانية الملوك",
        avatarType = "PRINCE",
        customAvatarUri = "content://media/external/images/media/101",
        customCoverUri = null,
        countryFlag = "🇪🇬",
        countryNameAr = "مصر",
        roleCode = "SUPPORTER",
        wealthLevel = 33,
        wealthExpCurrent = 12000L,
        wealthExpTarget = 20000L,
        charismaLevel = 27,
        charismaExpCurrent = 8500L,
        charismaExpTarget = 15000L,
        vipTier = 5,
        goldCoins = 500000L,
        crystalDiamonds = 120000L,
        equippedFrameId = "IMPERIAL_GOLD_WINGS",
        equippedWelcomeName = "تنين الإمبراطور الذهبي 7D"
      )
    )

    val savedUser = dao.getFirstSavedUser()
    assertNotNull(savedUser)
    assertEquals("الأمير محمد الملكي", savedUser?.nickname)
    assertEquals("content://media/external/images/media/101", savedUser?.customAvatarUri)
    assertEquals(33, savedUser?.wealthLevel)
    assertEquals(27, savedUser?.charismaLevel)

    dao.upsertRoomSettings(
      RoomSettingsEntity(
        roomId = "room_vip_1",
        roomDisplayId = "900101",
        titleAr = "ديوانية الملك محمد الرسمية",
        announcementAr = "أهلاً بكم في الغرفة الملكية",
        categoryAr = "شعبي",
        countryFlag = "🇪🇬",
        hostUserId = "user_my_account",
        hostName = "الأمير محمد الملكي",
        customCoverImageUri = "content://media/external/images/media/202",
        hostCustomAvatarUri = "content://media/external/images/media/101",
        backgroundStyleId = "PALACE_NIGHT",
        micShapeStyleId = "CIRCLE",
        onlineCount = 520,
        heatScore = 95000L
      )
    )

    val savedRooms = dao.getAllRoomSettings()
    assertEquals(1, savedRooms.size)
    assertEquals("ديوانية الملك محمد الرسمية", savedRooms.first().titleAr)
    assertEquals("content://media/external/images/media/202", savedRooms.first().customCoverImageUri)

    dao.insertChatHistoryMessage(
      ChatHistoryEntity(
        messageId = "msg_test_1",
        chatScope = "ROOM",
        roomId = "room_vip_1",
        senderUserId = "user_my_account",
        senderDisplayId = "10001",
        senderName = "الأمير محمد الملكي",
        senderCustomAvatarUri = "content://media/external/images/media/101",
        senderAvatarType = "PRINCE",
        senderVip = 5,
        senderWealthLevel = 33,
        senderCharismaLevel = 27,
        messageText = "مرحباً بالجميع في الغرفة!",
        timestampText = "الآن"
      )
    )

    val roomHistory = dao.getAllRoomChatHistory()
    assertEquals(1, roomHistory.size)
    assertEquals("مرحباً بالجميع في الغرفة!", roomHistory.first().messageText)

    dao.upsertHomeBanners(
      listOf(
        HomeBannerEntity(
          bannerId = "banner_db_1",
          titleAr = "حدث القصر الإمبراطوري",
          subtitleAr = "جوائز وهدايا ملكية",
          badgeTextAr = "حصري",
          iconEmoji = "👑",
          customImageUri = null,
          actionTargetScreen = "EVENT_CENTER",
          primaryColorHex = "#6A1B9A",
          secondaryColorHex = "#AD1457",
          accentGoldHex = "#FFD700",
          sortOrder = 1,
          isActive = true
        )
      )
    )
    assertEquals(1, dao.getAllHomeBanners().size)

    db.close()
  }
}
