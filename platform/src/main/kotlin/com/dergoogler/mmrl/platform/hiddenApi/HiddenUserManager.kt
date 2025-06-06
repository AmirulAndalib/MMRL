package com.dergoogler.mmrl.platform.hiddenApi

import android.content.Context
import android.content.pm.UserInfo
import android.os.IUserManager
import android.os.Process
import com.dergoogler.mmrl.platform.PlatformManager.getSystemService
import com.dergoogler.mmrl.platform.stub.IServiceManager

class HiddenUserManager(
    private val service: IServiceManager,
) {
    private val userManager by lazy {
        IUserManager.Stub.asInterface(
            service.getSystemService(Context.USER_SERVICE)
        )
    }

    fun getUsers(): List<UserInfo> = userManager.getUsers(true, true, true)

    fun getUserInfo(userId: Int): UserInfo = userManager.getUserInfo(userId)

    fun getUserId(uid: Int): Int = uid / PER_USER_RANGE

    val myUserId get(): Int = getUserId(Process.myUid())

    fun isSameUser(uid1: Int, uid2: Int): Boolean = getUserId(uid1) == getUserId(uid2)

    companion object {
        const val PER_USER_RANGE: Int = 100000
    }
}