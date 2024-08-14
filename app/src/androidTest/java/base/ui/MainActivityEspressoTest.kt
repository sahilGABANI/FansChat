package base.ui

import android.Manifest
import android.content.Context
import android.os.SystemClock
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import base.FansChatApplication
import base.R
import base.data.api.authentication.LoggedInUserCache
import base.di.FansChatAppModule
import base.util.language
import base.util.languageList
import junit.framework.AssertionFailedError
import org.hamcrest.CoreMatchers
import org.junit.*
import org.junit.runners.MethodSorters
import javax.inject.Inject
import kotlin.random.Random

@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MainActivityEspressoTest {

    private lateinit var app: FansChatApplication
    private lateinit var testAppComponent: TestAppComponent

    private val reportMessage: String = "Test report message from Espresso"
    private val postTitle: String = "Test Espresso"
    private val postDesc: String = "This post is created from Espresso test case"
    private val testPhoneEdited = "" + System.currentTimeMillis()
    private val postTitleEdited = "Edited Test Espresso"
    private val postComment = "Commented on post by espresso"
    private val postCommentEdited = "Edited comment on post by espresso"
    private val loginEmail = "test@mtn.com"
    private val loginPassword = "123456"
    private val loginFirstName = "Espresso"
    private val loginLastName = "MTN"
    private val loginNickName = "Espresso"
    private val loginPhone = "1234567890"

    @Inject
    lateinit var loggedInUserCache: LoggedInUserCache

    @Rule
    @JvmField
    val storagePermissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    @Rule
    @JvmField
    val activityRule: ActivityScenarioRule<MainActivity> =
        ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext<Context>() as FansChatApplication
        testAppComponent = DaggerTestAppComponent.builder()
            .fansChatAppModule(FansChatAppModule(app))
            .build()
        app.setAppComponent(testAppComponent)
        testAppComponent.inject(this)
    }

    @Test
    fun test_01_registerUser() {

        SystemClock.sleep(2000)

        if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {

            onView(
                CoreMatchers.allOf(
                    withId(R.id.register),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(1000)

            onView(
                CoreMatchers.allOf(
                    withId(R.id.firstname),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(loginFirstName), ViewActions.closeSoftKeyboard())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.lastname),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(loginLastName), ViewActions.closeSoftKeyboard())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.nickname),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(loginNickName), ViewActions.closeSoftKeyboard())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.email),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(loginEmail), ViewActions.closeSoftKeyboard())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.phone),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(loginPhone), ViewActions.closeSoftKeyboard())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.password),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(loginPassword), ViewActions.closeSoftKeyboard())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.register),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(2000)

            if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty())
                onView(
                    CoreMatchers.allOf(
                        withId(R.id.acceptButton),
                        isDisplayed()
                    )
                ).perform(ViewActions.click())

        }
    }

    @Test
    fun test_02_loginUser() {

        SystemClock.sleep(2000)

        if (loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {

            onView(
                CoreMatchers.allOf(
                    withId(R.id.login),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(1000)

            onView(
                CoreMatchers.allOf(
                    withId(R.id.emailIdEditTextView),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(loginEmail), ViewActions.closeSoftKeyboard())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.passwordEditTextView),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(loginPassword), ViewActions.closeSoftKeyboard())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.loginButton),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(2000)

            Assert.assertTrue(!loggedInUserCache.getLoginUserToken().isNullOrEmpty())
        }
    }

    @Test
    fun test_03_createPost() {
        SystemClock.sleep(2000)

        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {
            createPost()
        } else {

            onView(
                CoreMatchers.allOf(
                    withId(R.id.createPost),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.login),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(2000)

            onView(
                CoreMatchers.allOf(
                    withId(R.id.email),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(loginEmail), ViewActions.closeSoftKeyboard())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.password),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(loginPassword), ViewActions.closeSoftKeyboard())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.login),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(2000)

            onView(
                CoreMatchers.allOf(
                    withId(R.id.acceptButton),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(2000)

            createPost()
        }
    }

    private fun createPost() {
        onView(
            CoreMatchers.allOf(
                withId(R.id.createPost),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        onView(
            CoreMatchers.allOf(
                withId(R.id.title),
                isDisplayed()
            )
        ).perform(ViewActions.typeText(postTitle), ViewActions.closeSoftKeyboard())

        onView(
            CoreMatchers.allOf(
                withId(R.id.body),
                isDisplayed()
            )
        ).perform(
            ViewActions.typeText(postDesc),
            ViewActions.closeSoftKeyboard()
        )

        onView(
            CoreMatchers.allOf(
                withId(R.id.btnPost),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        seePost()
    }

    private fun seePost() {

        SystemClock.sleep(2000)

        onView(withId(R.id.rvWall))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(4))

        SystemClock.sleep(500)

        onView(withId(R.id.rvWall))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(5))

        SystemClock.sleep(500)

        onView(withId(R.id.rvWall))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(5))

        SystemClock.sleep(1000)
    }

    @Test
    fun test_04_likePost() {
        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {

            seePost()

            onView(withId(R.id.rvFeedWall)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    ViewActions.click()
                )
            )

            SystemClock.sleep(3000)

            onView(withId(R.id.like)).perform(ViewActions.click())

            SystemClock.sleep(1000)

            onView(withId(R.id.like)).perform(ViewActions.click())

            SystemClock.sleep(1000)

        }
    }

    @Test
    fun test_05_editPost() {
        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {

            seePost()

            onView(withId(R.id.rvFeedWall)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    ViewActions.click()
                )
            )

            SystemClock.sleep(3000)

            //Edit post

            onView(
                CoreMatchers.allOf(
                    withId(R.id.edit),
                    isDisplayed(),
                    ViewMatchers.isDescendantOfA(withId(R.id.post_actions))
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(500)

            onView(
                CoreMatchers.allOf(
                    withId(R.id.title),
                    isDisplayed()
                )
            ).perform(ViewActions.clearText())

            onView(
                CoreMatchers.allOf(
                    withId(R.id.title),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(postTitleEdited), ViewActions.closeSoftKeyboard())

            SystemClock.sleep(500)

            onView(withId(R.id.tvSave)).perform(ViewActions.click())

            SystemClock.sleep(2000)

            ViewActions.pressBack()

            SystemClock.sleep(2000)

        }
    }

    @Test
    fun test_06_addComment() {
        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {

            seePost()

            onView(withId(R.id.rvFeedWall)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    ViewActions.click()
                )
            )

            SystemClock.sleep(3000)

            //Add Comment
            onView(
                CoreMatchers.allOf(
                    withId(R.id.input),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(postComment), ViewActions.closeSoftKeyboard())

            SystemClock.sleep(500)

            onView(withId(R.id.send)).perform(ViewActions.click())

            SystemClock.sleep(2000)
        }
    }

    @Test
    fun test_07_editComment() {
        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {

            seePost()

            onView(withId(R.id.rvFeedWall)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    ViewActions.click()
                )
            )

            SystemClock.sleep(3000)

            //Edit Comment
            onView(withId(R.id.list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    RecyclerViewMatcher(R.id.list).clickOnViewChild(R.id.edit)
                )
            )

            SystemClock.sleep(500)

            onView(
                CoreMatchers.allOf(
                    withId(R.id.input),
                    isDisplayed()
                )
            ).perform(ViewActions.clearText())

            SystemClock.sleep(500)

            onView(
                CoreMatchers.allOf(
                    withId(R.id.input),
                    isDisplayed()
                )
            ).perform(ViewActions.typeText(postCommentEdited), ViewActions.closeSoftKeyboard())

            SystemClock.sleep(500)

            onView(withId(R.id.send)).perform(ViewActions.click())

            SystemClock.sleep(2000)

        }
    }

    @Test
    fun test_08_deleteComment() {
        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {

            seePost()

            onView(withId(R.id.rvFeedWall)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    ViewActions.click()
                )
            )
            SystemClock.sleep(3000)

            //Delete Comment
            try {

                onView(withId(R.id.list)).perform(
                    RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                        0,
                        RecyclerViewMatcher(R.id.list).clickOnViewChild(R.id.delete)
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            SystemClock.sleep(2000)
        }
    }

    @Test
    fun test_09_deletePost() {
        if (!loggedInUserCache.getLoginUserToken().isNullOrEmpty()) {

            seePost()

            onView(withId(R.id.rvFeedWall)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    ViewActions.click()
                )
            )
            SystemClock.sleep(3000)

            //Delete Post
            onView(
                CoreMatchers.allOf(
                    withId(R.id.delete),
                    isDisplayed(),
                    ViewMatchers.isDescendantOfA(withId(R.id.post_actions))
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(1000)

            onView(withId(R.id.ivOkay))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(isDisplayed()))
                .perform(ViewActions.click())

            SystemClock.sleep(3000)
        }
    }

    @Test
    fun test_10_openChat() {

        SystemClock.sleep(2000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.chat),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(3000)
    }

    @Test
    fun test_11_translateNews() {

        SystemClock.sleep(2000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.news),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(3000)

        onView(withId(R.id.list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                ViewActions.click()
            )
        )
        SystemClock.sleep(2000)

        //Translate Post
        onView(
            CoreMatchers.allOf(
                withId(R.id.translate),
                isDisplayed(),
                ViewMatchers.isDescendantOfA(withId(R.id.post_actions))
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(500)

        onData(CoreMatchers.anything()).inAdapterView(withId(R.id.listView))
            .atPosition(0).perform(
                ViewActions.click()
            )

        SystemClock.sleep(2000)
    }

    @Test
    fun test_12_reportSocial() {

        SystemClock.sleep(2000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.social),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(3000)

        onView(withId(R.id.list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                ViewActions.click()
            )
        )
        SystemClock.sleep(2000)

        //Report Post
        onView(
            CoreMatchers.allOf(
                withId(R.id.reportPost),
                isDisplayed(),
                ViewMatchers.isDescendantOfA(withId(R.id.post_actions))
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(500)

        onView(withId(R.id.reportAppCompatEditText)).perform(
            ViewActions.typeText(reportMessage),
            ViewActions.closeSoftKeyboard()
        )

        SystemClock.sleep(500)

        onView(withId(R.id.ivOkay))
            .inRoot(RootMatchers.isDialog())
            .check(ViewAssertions.matches(isDisplayed()))
            .perform(ViewActions.click())

        SystemClock.sleep(2000)
    }

    @Test
    fun test_13_editProfile() {

        SystemClock.sleep(2000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.menu),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(3000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.profile),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(2000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.editProfile),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(2000)

        onView(withId(R.id.phone)).perform(ViewActions.clearText())

        SystemClock.sleep(500)

        onView(withId(R.id.phone)).perform(
            ViewActions.typeText(testPhoneEdited),
            ViewActions.closeSoftKeyboard()
        )

        SystemClock.sleep(1000)

        onView(withId(R.id.save)).perform(ViewActions.click())

        SystemClock.sleep(3000)

        pressBack()

        SystemClock.sleep(3000)

    }

    @Test
    fun test_14_UnFriend() {

        SystemClock.sleep(2000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.showFriends),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(2000)

        onView(withId(R.id.list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                ViewActions.click()
            )
        )

        SystemClock.sleep(1000)

        try {
            //unfriend - used same button for send request and unfriend user
            onView(
                CoreMatchers.allOf(
                    withId(R.id.btnSendFriendReq),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(1000)

            onView(withId(R.id.ivOkay))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(isDisplayed()))
                .perform(ViewActions.click())

            SystemClock.sleep(1000)

        } catch (e: AssertionFailedError) {
            e.printStackTrace()
        }

        SystemClock.sleep(1000)
    }

    @Test
    fun test_15_reportUser() {

        SystemClock.sleep(2000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.showFriends),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(1000)

        try {

            onView(withId(R.id.list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    ViewActions.click()
                )
            )

            SystemClock.sleep(1000)

            onView(
                CoreMatchers.allOf(
                    withId(R.id.btnReport),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(500)

            onView(withId(R.id.reportAppCompatEditText)).perform(
                ViewActions.typeText(reportMessage),
                ViewActions.closeSoftKeyboard()
            )

            SystemClock.sleep(500)

            onView(withId(R.id.ivOkay))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(isDisplayed()))
                .perform(ViewActions.click())

            SystemClock.sleep(1000)

        } catch (e: AssertionFailedError) {
            e.printStackTrace()
        }

        SystemClock.sleep(1000)
    }

    @Test
    fun test_16_sendFriendRequest() {

        SystemClock.sleep(2000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.showFriends),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(1000)

        onView(
            CoreMatchers.allOf(
                ViewMatchers.withText(app.getString(R.string.find)),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(1000)

        onView(withId(R.id.list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                ViewActions.click()
            )
        )

        SystemClock.sleep(1000)

        try {

            onView(
                CoreMatchers.allOf(
                    withId(R.id.btnSendFriendReq),
                    isDisplayed()
                )
            ).perform(ViewActions.click())

            SystemClock.sleep(1000)

            onView(withId(R.id.ivOkay))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(isDisplayed()))
                .perform(ViewActions.click())

            SystemClock.sleep(1000)

        } catch (e: Exception) {
            try {

                onView(
                    CoreMatchers.allOf(
                        withId(R.id.ibAccept),
                        isDisplayed()
                    )
                ).perform(ViewActions.click())

                onView(withId(R.id.ivOkay))
                    .inRoot(RootMatchers.isDialog())
                    .check(ViewAssertions.matches(isDisplayed()))
                    .perform(ViewActions.click())

                SystemClock.sleep(1000)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        SystemClock.sleep(1000)
    }

    @Test
    fun test_17_acceptFriendRequest() {

        SystemClock.sleep(2000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.showFriends),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(1000)

        onView(
            CoreMatchers.allOf(
                ViewMatchers.withText(app.getString(R.string.requests)),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(1000)

        try {

            onView(withId(R.id.list)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    RecyclerViewMatcher(R.id.list).clickOnViewChild(R.id.acceptButton)
                )
            )

            SystemClock.sleep(1000)

            onView(withId(R.id.ivOkay))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(isDisplayed()))
                .perform(ViewActions.click())

            SystemClock.sleep(1000)

        } catch (e: Exception) {
            e.printStackTrace()
        }

        SystemClock.sleep(1000)
    }

    @Test
    fun test_18_openNotifications() {

        SystemClock.sleep(2000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.notifications),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(1500)

        try {

            onView(withId(R.id.rvNotifications)).perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    ViewActions.click()
                )
            )
            SystemClock.sleep(2000)

            pressBack()

        } catch (e: Exception) {
            e.printStackTrace()
        }

        SystemClock.sleep(1000)
    }

    //Keep this last as text comparison may fail after changing language
    @Test
    fun test_19_changeLanguage() {

        SystemClock.sleep(3000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.menu),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(3000)

        onView(
            CoreMatchers.allOf(
                withId(R.id.profile),
                isDisplayed()
            )
        ).perform(ViewActions.click())

        SystemClock.sleep(2000)

        onView(withId(R.id.switchAutoTranslate)).perform(ViewActions.click())

        SystemClock.sleep(500)

        onView(withId(R.id.switchNewsNoti)).perform(ViewActions.click())

        SystemClock.sleep(1000)

        onView(withId(R.id.languages)).perform(ViewActions.click())

        SystemClock.sleep(1000)

        //Select random language from list
        val indexToSelect = Random.nextInt(languageList.size)

        onView(withId(R.id.list)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                indexToSelect,
                ViewActions.click()
            )
        )

        SystemClock.sleep(1000)

        Assert.assertTrue(app.language == languageList[indexToSelect].shortCode)

        SystemClock.sleep(2000)

    }

}