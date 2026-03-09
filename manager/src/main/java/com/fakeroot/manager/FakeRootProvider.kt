package com.fakeroot.manager

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder

/**
 * Content Provider for receiving the FakeRoot binder from the server.
 */
class FakeRootProvider : ContentProvider() {

    companion object {
        const val METHOD_SEND_BINDER = "sendBinder"
        const val KEY_BINDER = "com.fakeroot.intent.extra.BINDER"
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        if (method == METHOD_SEND_BINDER && extras != null) {
            // Receive the binder from the server
            val binder = extras.getBinder(KEY_BINDER)
            if (binder != null) {
                // Store the binder for later use
                FakeRootApplication.instance.onBinderReceived(binder)
                return Bundle().apply { putBoolean("success", true) }
            }
        }
        return super.call(method, arg, extras)
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
