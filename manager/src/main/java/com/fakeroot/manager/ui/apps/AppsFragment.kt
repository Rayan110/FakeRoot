package com.fakeroot.manager.ui.apps

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fakeroot.manager.R
import com.fakeroot.manager.databinding.ItemAppBinding
import com.google.android.material.textview.MaterialTextView

class AppsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private val authorizedApps = mutableListOf<AppInfo>()
    private lateinit var adapter: AppsAdapter

    data class AppInfo(
        val packageName: String,
        val appName: String,
        val icon: android.graphics.drawable.Drawable,
        var isAuthorized: Boolean
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        tvEmpty = view.findViewById(R.id.tv_empty)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AppsAdapter()
        recyclerView.adapter = adapter

        loadApps()
    }

    private fun loadApps() {
        authorizedApps.clear()

        val pm = requireContext().packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)

        for (pkg in packages) {
            // Check if app requests our permission
            if (pkg.requestedPermissions?.contains("com.fakeroot.permission.API") == true) {
                val appInfo = AppInfo(
                    packageName = pkg.packageName,
                    appName = pm.getApplicationLabel(pkg.applicationInfo).toString(),
                    icon = pm.getApplicationIcon(pkg.applicationInfo),
                    isAuthorized = false // TODO: Load from preferences
                )
                authorizedApps.add(appInfo)
            }
        }

        // Add some demo apps for testing
        if (authorizedApps.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
        }
    }

    inner class AppsAdapter : RecyclerView.Adapter<AppsAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivIcon: ImageView = view.findViewById(R.id.iv_icon)
            val tvName: TextView = view.findViewById(R.id.tv_name)
            val tvPackage: TextView = view.findViewById(R.id.tv_package)
            val switchPermission: com.google.android.material.materialswitch.MaterialSwitch =
                view.findViewById(R.id.switch_permission)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = authorizedApps[position]
            holder.ivIcon.setImageDrawable(app.icon)
            holder.tvName.text = app.appName
            holder.tvPackage.text = app.packageName
            holder.switchPermission.isChecked = app.isAuthorized

            holder.switchPermission.setOnCheckedChangeListener { _, isChecked ->
                authorizedApps[position].isAuthorized = isChecked
                // TODO: Save to preferences
            }
        }

        override fun getItemCount() = authorizedApps.size
    }
}
