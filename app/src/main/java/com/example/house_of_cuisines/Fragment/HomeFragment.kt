package com.example.house_of_cuisines.Fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.house_of_cuisines.Activity.*
import com.example.house_of_cuisines.Adapter.DashboardAdapter
import com.example.house_of_cuisines.Adapter.HomeExpenseAdapter
import com.example.house_of_cuisines.Adapter.HomeSaleAdapter
import com.example.house_of_cuisines.ApiHelper.ApiController
import com.example.house_of_cuisines.ApiHelper.ApiResponseListner
import com.example.house_of_cuisines.Model.*
import com.example.house_of_cuisines.R
import com.example.house_of_cuisines.Utills.*
import com.google.gson.JsonElement
import com.stpl.antimatter.Utils.ApiContants

class HomeFragment : Fragment(), ApiResponseListner {

    private lateinit var apiClient: ApiController
    private var _binding: com.example.house_of_cuisines.databinding.FragmentHomeBinding? =
        null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = com.example.house_of_cuisines.databinding.FragmentHomeBinding.inflate(
            inflater,
            container,
            false
        )

        val root: View = binding.root
        apiClient = ApiController(activity, this)


     /*   val titleText = (activity as DashboardActivity?)
        titleText?.setTitle("Dashboard")*/

        binding.refreshLayout.setOnRefreshListener {
            apiDashboard()
            binding.refreshLayout.isRefreshing = false
        }
        binding.fbAddArchitect.setOnClickListener {
            requireActivity().startActivity(
                Intent(
                    requireActivity(),
                    SettingsActivity::class.java
                ).putExtra("way", "Add Expenses")
            )
        }
        //    ApiContants.movabalebutton(binding.fbAddArchitect,requireActivity())

        apiWalletLadger()

        binding.apply {
            cardAddSale.setOnClickListener {
                requireActivity().startActivity(
                    Intent(
                        requireActivity(),
                        AddEventActivity::class.java
                    ).putExtra("way", "Add Sale")
                )
            }
            cardAddExpense.setOnClickListener {
                requireActivity().startActivity(
                    Intent(
                        requireActivity(),
                        AddExpensesActivity::class.java
                    ).putExtra("way", "Add Expenses")
                )
            }
        }
        Glide.with(this).load(PrefManager.getString("CompanyLogo","")).into(binding.cmpnyLogo)


        return root
    }

    fun apiWalletLadger() {
        SalesApp.isAddAccessToken = true
        val params = Utility.getParmMap()
        apiClient.progressView.showLoader()
        params["fromDt"] = ""
        params["toDt"] = ""
        apiClient.getApiPostCall(ApiContants.GetWalletLadger, params)
    }
    fun apiDashboard() {
        SalesApp.isAddAccessToken = true
        val params = Utility.getParmMap()
        apiClient.progressView.showLoader()
        apiClient.getApiPostCall(ApiContants.Dashboard, params)
    }

    override fun success(tag: String?, jsonElement: JsonElement) {
        try {
            apiClient.progressView.hideLoader()

            if (tag == ApiContants.Dashboard) {
                val dashboardBean = apiClient.getConvertIntoModel<DashboardBean>(
                    jsonElement.toString(),
                    DashboardBean::class.java
                )
                if (dashboardBean.error == false) {
                    handleSalesList(dashboardBean.data.sales)
                    handleExpenseList(dashboardBean.data.expenses)
                    handleDashboardList(dashboardBean.data)
                }else{
                    Toast.makeText(requireContext(), dashboardBean.msg, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            if (tag == ApiContants.GetWalletLadger) {
                var walletLedgerBean = apiClient.getConvertIntoModel<WalletLedgerBean>(
                    jsonElement.toString(),
                    WalletLedgerBean::class.java
                )
                if (walletLedgerBean.error == false) {
                    (activity as DashboardActivity?)?.setWalletAmt(walletLedgerBean.data.userWallet.walletAmt.toString())
                    //     handleAllTask(walletLedgerBean.data.walletHistory)
                } else {
                    Toast.makeText(requireContext(), walletLedgerBean.msg, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } catch (e: Exception) {
            Log.d("error>>", e.localizedMessage)
        }
    }

    override fun failure(tag: String?, errorMessage: String) {
        apiClient.progressView.hideLoader()
        Utility.showSnackBar(requireActivity(), errorMessage)
    }

    fun handleDashboardList(data: DashboardBean.Data) {
        binding.rcDashboard.layoutManager =
            GridLayoutManager(requireContext(),2)
        val mAllAdapter = DashboardAdapter(requireActivity(), getDashDataList(data), object :
            RvStatusClickListner {
            override fun clickPos(status: String, pos: Int) {

            }
        })
        binding.rcDashboard.adapter = mAllAdapter
        mAllAdapter.notifyDataSetChanged()
        // rvMyAcFiled.isNestedScrollingEnabled = false
    }

    fun handleSalesList(data: List<DashboardBean.Data.Sale>) {
        binding.rcSale.layoutManager =
            LinearLayoutManager(requireContext())
        val mAllAdapter = HomeSaleAdapter(requireActivity(),data, object :
            RvStatusClickListner {
            override fun clickPos(status: String, pos: Int) {
            }
        })
        binding.rcSale.adapter = mAllAdapter
        mAllAdapter.notifyDataSetChanged()
        // rvMyAcFiled.isNestedScrollingEnabled = false
    }

    fun handleExpenseList(data: List<DashboardBean.Data.Expense>) {
        binding.rcExpense.layoutManager =
            LinearLayoutManager(requireContext())
          val mAllAdapter = HomeExpenseAdapter(requireActivity(), data, object :
            RvStatusClickListner {
            override fun clickPos(status: String, pos: Int) {

            }
        })
        binding.rcExpense.adapter = mAllAdapter
        mAllAdapter.notifyDataSetChanged()
        // rvMyAcFiled.isNestedScrollingEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getDashDataList(data: DashboardBean.Data): ArrayList<MenuModelBean> {
        var menuList = ArrayList<MenuModelBean>()
        //  menuList.add(MenuModelBean(1, "Add Lead", data.newLeads.toString(), R.drawable.ic_dashbord))
        menuList.add(MenuModelBean(2, "Total Customer", data.totalCustomer.totalCustomer.toString(), R.drawable.ic_dashbord))
        menuList.add(
            MenuModelBean(
                3,
                "Total Vendor",
                data.totalVendor.totalVendor.toString(),
                R.drawable.ic_dashbord
            )
        )

        menuList.add(
            MenuModelBean(
                4,
                "Total Event",
                data.totalSale.totalSale.toString(),
                R.drawable.ic_dashbord
            )
        )
        menuList.add(
            MenuModelBean(
                5,
                "Total Expense",
                data.totalExpense.totalExpense.toString(),
                R.drawable.ic_dashbord
            )
        )


        return menuList
    }

    fun callPGURL(url: String) {
        Log.d("weburl", url)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.android.chrome")
        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        //   apiAllGet()
        apiDashboard()
    }

}