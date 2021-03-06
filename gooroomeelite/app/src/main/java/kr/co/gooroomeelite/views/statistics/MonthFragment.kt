package kr.co.gooroomeelite.views.statistics

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.firebase.firestore.FirebaseFirestore
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kr.co.gooroomeelite.R
import kr.co.gooroomeelite.adapter.DailySubjectAdapter
import kr.co.gooroomeelite.databinding.FragmentMonthBinding
import kr.co.gooroomeelite.entity.ReadSubejct
import kr.co.gooroomeelite.entity.Subjects
import kr.co.gooroomeelite.utils.LoginUtils
import kr.co.gooroomeelite.viewmodel.SubjectViewModel
import kr.co.gooroomeelite.views.statistics.share.ShareActivity
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
@RequiresApi(Build.VERSION_CODES.O)
class MonthFragment : Fragment() {
    private lateinit var binding: FragmentMonthBinding
    private val viewModel: SubjectViewModel by viewModels()

//    private lateinit var chart: BarChart
    private val dailySubjectAdapter: DailySubjectAdapter by lazy { DailySubjectAdapter(emptyList()) }


    //db??? ??????
    private lateinit var subjects: Subjects
    private var list: MutableList<Subjects> = mutableListOf()


//     LocalDate ???????????? ??????
//    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d")


    private val listData by lazy {
        mutableListOf(
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(5.5f)),
            ChartDatas("", arrayListOf(7.5f)),
            ChartDatas("", arrayListOf(8.5f)),
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(10.5f)),
            ChartDatas("", arrayListOf(18.5f)),
            ChartDatas("", arrayListOf(5.5f)),
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(1.5f)),
            ChartDatas("", arrayListOf(6.5f)),
            ChartDatas("", arrayListOf(19.5f)),
            ChartDatas("", arrayListOf(5.5f)),
            ChartDatas("", arrayListOf(9.5f)),
            ChartDatas("", arrayListOf(3.5f)),
            ChartDatas("", arrayListOf(16.5f)),
            ChartDatas("", arrayListOf(3.5f)),
            ChartDatas("", arrayListOf(12.5f)),
            ChartDatas("", arrayListOf(14.5f)),
            ChartDatas("", arrayListOf(15.5f)),
            ChartDatas("", arrayListOf(16.5f)),
            ChartDatas("", arrayListOf(17.5f)),
            ChartDatas("", arrayListOf(18.5f)),
            ChartDatas("", arrayListOf(12.5f))
        )
    }

    //??????,?????? ?????? ??????
    private val whiteColor by lazy {
        ContextCompat.getColor(this.requireContext(), R.color.black)
    }

    //????????? ?????? ???,??? (???????????? ??????)
    private val transparentBlackColor by lazy {
        ContextCompat.getColor(this.requireContext(), R.color.black)
    }

//    private val customMarkerView by lazy {
//        CustomMarketView(this.requireContext(), R.layout.item_marker_view)
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMonthBinding.inflate(inflater,container,false)
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_month,container,false)
        binding.month = this
//        val view = inflater.inflate(R.layout.fragment_month, container, false)

//        val shareButton: Button = view.findViewById(R.id.share_button)/
        binding.shareButton.setOnClickListener {
            requestPermission()
        }
        //??? ??????
//        chart = view.findViewById(R.id.month_bar_chart)
        binding.monthBarChart.setNoDataText("")
        initChart(binding.monthBarChart)
        binding.monthBarChart.setVisibleXRangeMaximum(30f)
        binding.monthBarChart.moveViewToX(30f)

        FirebaseFirestore.getInstance()
            .collection("subject")
            .whereEqualTo("uid", LoginUtils.getUid()!!)
            .get() //?????? ?????? ??? ?????? ?????? ????????????.
            .addOnSuccessListener { docs ->
                if(docs != null) {
                    lateinit var subjectValue: ReadSubejct
                    docs.documents.forEach {
                        subjectValue = ReadSubejct(it.toObject(Subjects::class.java)!!,it)
                        subjects = it.toObject(Subjects::class.java)!!
                        list.add(subjects)
                        monthlySubjectPieChart(binding.weeklyPieChart,list)
                        Log.d("aqaqList", list.size.toString())
                        //?????? ?????? ??????
                    }
                }
            }

//        var calendarMonth : TextView = view.findViewById(R.id.calendar_month)
//        var calRightBtn : ImageButton = view.findViewById(R.id.cal_right_btn)
//        var calLeftBtn : ImageButton = view.findViewById(R.id.cal_left_btn)
        binding.recyclerViewMonth.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = dailySubjectAdapter
        }

        moveCalendarByDay(binding.calendarMonth,binding.calRightBtn,binding.calLeftBtn)
        return binding.root
    }

    //adapter??? ????????? ??????
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.subjectList.observe(viewLifecycleOwner) {
            dailySubjectAdapter.setData(it)
        }
    }

    private fun monthlySubjectPieChart(pieChart : PieChart, list: MutableList<Subjects>){
        pieChart.setUsePercentValues(true)
        Log.d("qwqwqwqwqw",subjects.studytime.toString())
        Log.d("qwqwqwqwqw",subjects.color.toString())
        Log.d("aqaqAllList", list.size.toString())

        val values = mutableListOf<PieEntry>()
        val colorItems = mutableListOf<Int>()
        list.forEachIndexed{ index, _ ->
            values.add(PieEntry(list[index].studytime.toFloat(), list[index].name))
            colorItems.add(index,Color.parseColor(list[index].color))
        }

        val pieDataSet = PieDataSet(values,"")
        pieDataSet.colors = colorItems
        pieDataSet.apply {
//            valueTextColor = Color.BLACK
            setDrawValues(false) //????????? ???????????? ??? ?????????
            valueTextSize = 16f
        }
        //% : ????????? ?????? ????????? ????????? ??????
        val pieData = PieData(pieDataSet)
        pieChart.apply {
            data = pieData
            description.isEnabled = false //?????? ????????? ????????? ?????? ???????????? ????????? ????????????.
            isRotationEnabled = false //???????????? ??????????????? ?????? ??? ??????
//            centerText = "this is color" //????????? ??? ????????? ????????? ?????????
//            setEntryLabelColor(Color.RED) //????????? ???????????? ????????? ??? ??????
            isEnabled = false
            legend.isEnabled = false //?????? ?????????
            isDrawHoleEnabled = true //????????? ?????? ????????? ??????
            holeRadius = 50f //????????? ????????? ??? ?????????
            setDrawEntryLabels(false) //????????? ?????? ?????? ??????
            animateY(1400, Easing.EaseInOutQuad)
            animate()
        }

    }


    private fun moveCalendarByDay(calendarMonth:TextView,calRightBtn:ImageButton,calLeftBtn:ImageButton){
        // ?????? ??????/?????? ????????????
        val dateNow: LocalDate = LocalDate.now()
        val textformatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM")

        var count : Int = 0
        calendarMonth.text = dateNow.format(textformatter) //?????? 2021.07.08

        dateNow.plusDays(count.toLong()) //??????????????? ???????????? ??? ?????? ????????? ?????? ??????
        calRightBtn.setOnClickListener {
            count++
            val dayPlus: LocalDate = dateNow.plusMonths(count.toLong())
            calendarMonth.text =  dayPlus.format(textformatter).toString()
        }

        calLeftBtn.setOnClickListener {
            count--
            val minusDay: LocalDate = dateNow.plusMonths(count.toLong())
            calendarMonth.text =  minusDay.format(textformatter).toString()
        }
    }

    private fun requestPermission(): Boolean {
        var permissions = false
        TedPermission.with(context)
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    permissions = true      //p0=response(??????)
                    val shareIntent = Intent(context, ShareActivity::class.java)
                    startActivity(shareIntent)
//                    finish()
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    permissions = false
                }

            })
            .setDeniedMessage("?????? ??????????????? ????????? ????????????????????????.")
            .setPermissions(Manifest.permission.CAMERA)
            .check()
        return permissions
    }

    private fun initChart(chart: BarChart) {
//        customMarkerView.chartView = chart
        with(chart) {
//            marker = customMarkerView
            description.isEnabled = false
            legend.isEnabled = false
            isDoubleTapToZoomEnabled = false

            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(false)
            //?????? ????????? ??????
            val barChartRender = CustomBarChartRender(this, animator, viewPortHandler).apply {
//                setRadius(10)
            }
            renderer = barChartRender
        }
        setData(listData)
    }

    private fun setData(barData: List<ChartDatas>) {
        val values = mutableListOf<BarEntry>()
        barData.forEachIndexed { index, chartData ->
            //????????? ?????? x , ????????? ?????? y
            for(i in chartData.value){
                values.add(BarEntry(index.toFloat(), i))
            }
        }

        //?????? ????????? ?????? ??????
        val barDataSet = BarDataSet(values, "").apply {
            //??? ???????????? ?????? ????????? ???????????? ???????????? ??????  (y??? ???????????? ??????????????? ????????? true??? ???????????? ????????? ????????? false??? ????????????.)
            setDrawValues(false)

            val colors = ArrayList<Int>()
            colors.add(Color.argb(100,68,158,246))
            setColors(colors)
            highLightAlpha = 0
        }

        //?????? ????????? ?????? ??????
        val dataSets = mutableListOf(barDataSet)
        val data = BarData(dataSets as List<IBarDataSet>?).apply {
//            setValueTextSize(30F)
            barWidth = 0.5F
        }
        //??????????????? ?????? 0.1???
        with(binding.monthBarChart) {
            animateY(100)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
//                setVisibleXRangeMaximum(20f)    //?????? X?????? ???????????? ????????? ???????????? ???????????? ?????????
//                moveViewToX(60f)
//                setVisibleXRangeMaximum(100f)
                setDrawGridLines(false)
                textColor = whiteColor
                //??? ~ ???
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return barData[value.toInt()].date
                    }
                }
            }

            //?????? ?????? ???, Y?????? ( ?????? ?????????,????????? )
            axisRight.apply {
                textColor = whiteColor
                setDrawAxisLine(false) //??????
                gridColor = transparentBlackColor
                gridLineWidth = 0.5F
                enableGridDashedLine(5f,5f,5f)

                var count = 0
                //??????????????? ????????? ?????? ??? ???
                barData.forEachIndexed { index, chartData ->
                    for (i in chartData.value) {
//                        var chartDataMax = listData.maxBy { it -> it. }
                        var maxValue = i
                        Log.d("aaa", "$maxValue"+"maxValue???")
                        barData.forEachIndexed { index, chartData ->
                            while (i > axisMaximum) {
                                count++
                                if (i > axisMaximum) {
                                    axisMaximum = maxValue
                                } else {
                                    axisMaximum = 9F
                                }
                            }
                        }
                    }
                }
                axisMinimum = 0F
//                axisMaximum = 9F
                granularity  = 3F //30???????????? ?????? ???????????? granularity ????????? ??? ?????????
                //y??? ?????? ?????????
                valueFormatter = object : ValueFormatter() {
                    private val mFormat: DecimalFormat = DecimalFormat("###")
                    override fun getFormattedValue(value: Float): String {
                        return mFormat.format(value) + "???"
                    }
                }
            }

            //?????? ????????? ???, Y?????? false??????
            axisLeft.apply {
                isEnabled = false
                gridColor = transparentBlackColor
                var count = 0
                //??????????????? ????????? ?????? ??? ???
                barData.forEachIndexed { index, chartData ->
                    for (i in chartData.value) {
//                        var chartDataMax = listData.maxBy { it -> it. }
                        var maxValue = i
                        Log.d("aaa", "$maxValue")
                        while (i > axisMaximum) {
                            count++
                            if (i > axisMaximum) {
                                axisMaximum = maxValue
                            } else {
                                axisMaximum = 9F
                            }
                        }
                    }
                }
                axisMinimum = 3F
//                axisMaximum = 9F
            }

            notifyDataSetChanged()  //chart??? ??? ????????? ?????????
//            setVisibleXRangeMaximum(10f)    //?????? X?????? ???????????? ????????? ???????????? ???????????? ?????????
//            moveViewToX(10f)
//            setVisibleXRangeMaximum(10f)

            this.data = data
            invalidate()
        }
    }
}
//    private fun listDataValue(){
//        var i : Int = 1
//        while (i <= 31) {
//            val name : String = ""
//            mutableListOf(
//
//                ChartDatas(name,(Math.random()*16).toFloat())
//            )
//            i++
//            return
//        }
//    }