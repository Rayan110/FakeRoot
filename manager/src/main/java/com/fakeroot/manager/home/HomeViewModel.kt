package com.fakeroot.manager.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.fakeroot.manager.R
import com.fakeroot.manager.util.ShizukuHelper

/**
 * ViewModel for the Home screen
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _items = MutableLiveData<List<HomeItem>>()
    val items: LiveData<List<HomeItem>> = _items

    private val _serviceStatus = MutableLiveData<ServiceStatus>()
    val serviceStatus: LiveData<ServiceStatus> = _serviceStatus

    sealed class ServiceStatus {
        object Checking : ServiceStatus()
        object Running : ServiceStatus()       // IMQSNative available, root ready
        object NotRunning : ServiceStatus()    // Shizuku not available or no permission
        object NotAvailable : ServiceStatus()  // IMQSNative not available
    }

    fun loadItems() {
        Thread {
            checkServiceStatus()
            updateItems()
        }.start()
    }

    fun refresh() {
        Thread {
            checkServiceStatus()
            updateItems()
        }.start()
    }

    private fun checkServiceStatus() {
        _serviceStatus.postValue(ServiceStatus.Checking)

        // First check if Shizuku is available
        if (!ShizukuHelper.isShizukuAvailable()) {
            _serviceStatus.postValue(ServiceStatus.NotRunning)
            return
        }

        // Check if Shizuku has permission
        if (!ShizukuHelper.hasShizukuPermission()) {
            _serviceStatus.postValue(ServiceStatus.NotRunning)
            return
        }

        // Check if IMQSNative is available via Shizuku
        if (!ShizukuHelper.checkIMQSNativeAvailable()) {
            _serviceStatus.postValue(ServiceStatus.NotAvailable)
            return
        }

        // IMQSNative is available, root is ready
        _serviceStatus.postValue(ServiceStatus.Running)
    }

    private fun updateItems() {
        val itemList = mutableListOf<HomeItem>()

        // Add service status item
        when (_serviceStatus.value) {
            ServiceStatus.Running -> {
                itemList.add(HomeItem.Status(
                    titleRes = R.string.status_running,
                    summaryRes = R.string.status_running_summary,
                    statusColor = R.color.status_running
                ))
            }
            ServiceStatus.NotRunning -> {
                itemList.add(HomeItem.Status(
                    titleRes = R.string.status_not_running,
                    summaryRes = R.string.status_not_running_summary,
                    statusColor = R.color.status_not_running
                ))
            }
            ServiceStatus.NotAvailable -> {
                itemList.add(HomeItem.Status(
                    titleRes = R.string.status_not_available,
                    summaryRes = R.string.status_not_available_summary,
                    statusColor = R.color.status_error
                ))
            }
            else -> {
                itemList.add(HomeItem.Status(
                    titleRes = R.string.status_checking,
                    summaryRes = R.string.status_checking_summary,
                    statusColor = R.color.status_checking
                ))
            }
        }

        // Add manage apps button
        itemList.add(HomeItem.Action(
            titleRes = R.string.action_manage_apps,
            summaryRes = R.string.action_manage_apps_summary,
            action = HomeItem.ActionType.MANAGE_APPS
        ))

        // Add terminal button
        itemList.add(HomeItem.Action(
            titleRes = R.string.action_terminal,
            summaryRes = R.string.action_terminal_summary,
            action = HomeItem.ActionType.TERMINAL
        ))

        // Add settings button
        itemList.add(HomeItem.Action(
            titleRes = R.string.action_settings,
            summaryRes = R.string.action_settings_summary,
            action = HomeItem.ActionType.SETTINGS
        ))

        _items.postValue(itemList)
    }
}
