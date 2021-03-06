package kr.co.gooroomeelite.views.statistics

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
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
import kotlinx.android.synthetic.main.fragment_home.*
import kr.co.gooroomeelite.R
import kr.co.gooroomeelite.adapter.DailySubjectAdapter
import kr.co.gooroomeelite.databinding.FragmentWeekBinding
import kr.co.gooroomeelite.entity.ReadSubejct
import kr.co.gooroomeelite.entity.Subject
import kr.co.gooroomeelite.entity.Subjects
import kr.co.gooroomeelite.utils.LoginUtils
import kr.co.gooroomeelite.viewmodel.SubjectViewModel
import kr.co.gooroomeelite.views.statistics.share.ShareActivity
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class WeekFragment : Fragment() {
    private lateinit var binding : FragmentWeekBinding
    private val viewModel: SubjectViewModel by viewModels()
    private val myStudyTime = MutableLiveData<Int>()

    private val dailySubjectAdapter: DailySubjectAdapter by lazy { DailySubjectAdapter(emptyList()) }


    //db??? ??????
    private lateinit var subjects: Subjects
    private var list: MutableList<Subjects> = mutableListOf()

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

    // ?????? ??????/?????? ????????????
    val dateNow: LocalDateTime = LocalDateTime.now(     )
    //     LocalDate ???????????? ??????
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E")
//    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d")



    private val listData by lazy {
        mutableListOf(
            ChartData("???", 24.1f),
            ChartData("???", 9.5f),
            ChartData("???", 3.5f),
            ChartData("???", 7.5f),
            ChartData("???", 5.5f),
            ChartData("???", 12.5f),
            ChartData("???", 19.5f),
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWeekBinding.inflate(inflater,container,false)
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_week,container,false)
        binding.week = this

        binding.weekBarChart.setNoDataText("")
        initChart(binding.weekBarChart)

        FirebaseFirestore.getInstance()
            .collection("subject")
            .whereEqualTo("uid", LoginUtils.getUid()!!)
            .get() //?????? ?????? ??? ?????? ?????? ????????????.
            .addOnSuccessListener { docs ->
                if(docs != null) {
                    docs.documents.forEach {
                        subjects = it.toObject(Subjects::class.java)!!
                        list.add(subjects)
                        weeklySubjectPieChart(binding.weeklyPieChart,list)
//                        weelkyTotalHourTitle(list,subjects)
                    }
                }
            }

        
        binding.shareButton.setOnClickListener {
            requestPermission()
        }

        pieChartRecyclerView()
        //????????? ?????? ????????? ?????? ?????? ??????
        moveCalendarByWeek(binding.calendarMonday,binding.calendarSunday,binding.calRightBtn,binding.calLeftBtn)
        return binding.root
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
//                setRadius(20)
            }
            renderer = barChartRender
        }
        setData(listData)
    }

    private fun setData(barData: List<ChartData>) {

        val values = mutableListOf<BarEntry>()

        barData.forEachIndexed { index, chartData ->
            //????????? ?????? x , ????????? ?????? y
                values.add(BarEntry(index.toFloat(), chartData.value))
        }

        //?????? ????????? ?????? ??????
        val barDataSet = BarDataSet(values, "").apply {
            setDrawValues(false)

            val colors = ArrayList<Int>()
            colors.add(Color.argb(100,51, 155, 255))
            setColors(colors)
            highLightAlpha = 0
        }

        //?????? ????????? ?????? ??????
        val dataSets = mutableListOf(barDataSet)
        val data = BarData(dataSets as List<IBarDataSet>?).apply {
//            setValueTextSize(30F)
            barWidth = 0.3F
        }
        //??????????????? ?????? 0.1???
        with(binding.weekBarChart) {

            //????????? 7?????? ????????? ???
//        chart.setVisibleXRangeMaximum(7f)
//        chart.setVisibleXRangeMinimum(7f)???//????????? ????????
            //?????? ?????? ????????? ????????? x ??? ????????? ???????????????.
//        chart.moveViewToX(-10f)
//        chart.moveViewTo(100f,0f, YAxis.AxisDependency.LEFT)
//        val shareButton: Button = view.findViewById(R.id.share_button)
//            setVisibleXRangeMaximum(7f)
//            chart.setVisibleXRangeMaximum(7f)
//        chart.setVisibleXRangeMinimum(7f)???//????????? ????????
            animateY(100)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false) //??? ??????
                textColor = whiteColor
                //??? ~ ???
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return barData[value.toInt()].date
                    }
                }
            }

//            axisLeft.labelPosition = true
//            axisLeft.labelCount =
//            axisLeft.setPosition(floatArrayOf(0f, 10f, 20f, 50f, 100f, 300f))
            //?????? ?????? ???, Y?????? ( ?????? ?????????,????????? )
            axisRight.apply {
                textColor = whiteColor
                setDrawAxisLine(false) //??????
                gridColor = transparentBlackColor
                gridLineWidth = 0.5F
                enableGridDashedLine(5f,5f,5f)

//                axisMaximum = 24F //?????????
//                granularity  = 6F //30???????????? ?????? ???????????? granularity ????????? ??? ?????????
//                setLabelCount(6,true) //??? ????????????
//                axisMinimum = 3F //?????????
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
                //??????????????? ????????? ?????? ??? ???
//                var chartDataMax = listData.maxBy { it -> it.value}
//                barData.forEachIndexed { index, chartData ->
//                    for (i in chartData.value) {
////                        var chartDataMax = listData.maxBy { it -> it. }
//                        var maxValue = i
//                        Log.d("aaa", "$maxValue")
//                        while (i > axisMaximum) {
//                            count++
//                            if (i > axisMaximum) {
//                                axisMaximum = maxValue
//                            } else {
//                                axisMaximum = 9F
//                            }
//                        }
//                    }
//                }
                    setLabelCount(6,true) //??? ????????????
                    axisMaximum = 24F //?????????
                    granularity  = 6F //30???????????? ?????? ???????????? granularity ????????? ??? ?????????
                    axisMinimum = 3F //?????????
//

            }
            notifyDataSetChanged()
            this.data = data
            invalidate()
        }
    }

//    private fun  weelkyTotalHourTitle(list:MutableList<Subjects>,subjects: Subjects){
//        val calendarWeekNow : LocalDateTime = LocalDateTime.now()
//        val monday : LocalDateTime = LocalDateTime.now().with(DayOfWeek.MONDAY)//?????? ????????? ?????????
//        val sunday : LocalDateTime = LocalDateTime.now().with(DayOfWeek.SUNDAY)
//        Log.d("localDataTime",monday.toString())
//        Log.d("localDataTime",sunday.toString())
//        Log.d("localDataTime",list.get(5).toString())
//        Log.d("localDataTime",subjects.timestamp.toString())
//    }

    private fun moveCalendarByWeek(monDay:TextView,sunDay:TextView,rBtn:ImageButton,lBtn:ImageButton){
        val calendarWeekNow : LocalDateTime = LocalDateTime.now()
        val monday : LocalDateTime = LocalDateTime.now().with(DayOfWeek.MONDAY)//?????? ????????? ?????????
        val sunday : LocalDateTime = LocalDateTime.now().with(DayOfWeek.SUNDAY)

        val textformatter : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        var count: Int = 0

        monDay.text = monday.format(textformatter)+" "
        sunDay.text = "~ " + sunday.format(textformatter)

        rBtn.setOnClickListener{
            count++
            val mondayValue : LocalDateTime = monday.plusWeeks(count.toLong())
            monDay.text = mondayValue.format(textformatter).toString()
            val sundayValue : LocalDateTime = sunday.plusWeeks(count.toLong())
            sunDay.text = sundayValue.format(textformatter).toString()
        }

        lBtn.setOnClickListener{
            count--
            val sundayValue : LocalDateTime = sunday.plusWeeks(count.toLong())
            sunDay.text = sundayValue.format(textformatter).toString()
            val mondayValue : LocalDateTime = monday.plusWeeks(count.toLong())
            monDay.text = mondayValue.format(textformatter).toString()
        }

    }

    //????????? ??? ??????
    private fun weeklySubjectPieChart(pieChart : PieChart, list: MutableList<Subjects>){
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
    private fun pieChartRecyclerView(){
        binding.recyclerViewWeek.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            adapter = dailySubjectAdapter
        }
    }
    //adapter??? ????????? ??????
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.subjectList.observe(viewLifecycleOwner) {
            dailySubjectAdapter.setData(it)
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


}

//BEFORE CODE
//class WeekFragment : Fragment() {
//    private lateinit var binding : FragmentWeekBinding
//    private val viewModel: SubjectViewModel by viewModels()
//    //    private lateinit var chart: BarChart
//    private val myStudyTime = MutableLiveData<Int>()
//
//    private val dailySubjectAdapter: DailySubjectAdapter by lazy { DailySubjectAdapter(emptyList()) }
//
//
//    //db??? ??????
//    private lateinit var subjects: Subjects
//    private var list: MutableList<Subjects> = mutableListOf()
//
//
//    //??????,?????? ?????? ??????
//    private val whiteColor by lazy {
//        ContextCompat.getColor(this.requireContext(), R.color.black)
//    }
//
//    //????????? ?????? ???,??? (???????????? ??????)
//    private val transparentBlackColor by lazy {
//        ContextCompat.getColor(this.requireContext(), R.color.black)
//    }
//
////    private val customMarkerView by lazy {
////        CustomMarketView(this.requireContext(), R.layout.item_marker_view)
////    }
//
//    // ?????? ??????/?????? ????????????
//    val dateNow: LocalDateTime = LocalDateTime.now(     )
//    //     LocalDate ???????????? ??????
//    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("E")
////    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M/d")
//
//
//
//    private val listData by lazy {
//        mutableListOf(
//            ChartData("???", (24.1f)),
//            ChartData("???", (9.5f)),
//            ChartData("???", (3.5f)),
//            ChartData("???", (7.5f)),
//            ChartData("???", (5.5f)),
//            ChartData("???", (12.5f)),
//            ChartData("???", (19.5f))
//        )
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentWeekBinding.inflate(inflater,container,false)
//        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_week,container,false)
//        binding.week = this
//
////        val view = inflater.inflate(R.layout.fragment_week, container, false)
//
//        //??? ??????
////        chart = view.findViewById(R.id.week_bar_chart)
//        binding.weekBarChart.setNoDataText("")
//        initChart(binding.weekBarChart)
//
//        FirebaseFirestore.getInstance()
//            .collection("subject")
//            .whereEqualTo("uid", LoginUtils.getUid()!!)
//            .get() //?????? ?????? ??? ?????? ?????? ????????????.
//            .addOnSuccessListener { docs ->
//                if(docs != null) {
//                    lateinit var subjectValue: ReadSubejct
//                    docs.documents.forEach {
//                        subjects = it.toObject(Subjects::class.java)!!
//                        list.add(subjects)
////                        Log.d("qqMutableList;Subjects", list.toString())
////                        Log.d("qqMutableList;Subjects",list.get(0).timestamp.toString())
////                        Log.d("qqMutableList;Subjects", list.size.toString())
////                        subjects: Subjects
////                        private var list: MutableList<Subjects> = mutableListOf()
//                        weeklySubjectPieChart(binding.weeklyPieChart,list)
//
////                        val subject = it.toObjects(Subject::class.java)
////                        var studytimetodaylist = mutableListOf<Int>()
////                        for (i in 0..subject.size - 1) {
////                            studytimetodaylist.add(subject[i].studytime)
////                        }
////                        todayStudyTime.value = studytimetodaylist.sum()
////                        FirebaseFirestore.getInstance().collection("users").document(getUid()!!).update("todaystudytime",todayStudyTime.value)
//                    }
//                }
//            }
//
//        //????????? 7?????? ????????? ???
////        chart.setVisibleXRangeMaximum(7f)
////        chart.setVisibleXRangeMinimum(7f)???//????????? ????????
//        //?????? ?????? ????????? ????????? x ??? ????????? ???????????????.
////        chart.moveViewToX(-10f)
////        chart.moveViewTo(100f,0f, YAxis.AxisDependency.LEFT)
////        val shareButton: Button = view.findViewById(R.id.share_button)
//        binding.shareButton.setOnClickListener {
//            requestPermission()
//        }
//
//        binding.recyclerViewWeek.apply {
//            layoutManager = LinearLayoutManager(
//                requireContext(),
//                LinearLayoutManager.VERTICAL,
//                false
//            )
//            adapter = dailySubjectAdapter
//        }
//
//        moveCalendarByWeek(binding.calendarMonday,binding.calendarSunday,binding.calRightBtn,binding.calLeftBtn)
//        return binding.root
//    }
//
//    //adapter??? ????????? ??????
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        viewModel.subjectList.observe(viewLifecycleOwner) {
//            dailySubjectAdapter.setData(it)
//        }
//    }
//
//    private fun initChart(chart: BarChart) {
////        customMarkerView.chartView = chart
//        with(chart) {
////            marker = customMarkerView
//            description.isEnabled = false
//            legend.isEnabled = false
//            isDoubleTapToZoomEnabled = false
//
//            setPinchZoom(false)
//            setDrawBarShadow(false)
//            setDrawValueAboveBar(false)
//            //?????? ????????? ??????
//            val barChartRender = CustomBarChartRender(this, animator, viewPortHandler).apply {
////                setRadius(20)
//            }
//            renderer = barChartRender
//        }
//        setData(listData)
//    }
//
//    private fun setData(barData: List<ChartData>) {
//
//        val values = mutableListOf<BarEntry>()
//
//        barData.forEachIndexed { index, chartData ->
//            //????????? ?????? x , ????????? ?????? y
//            values.add(BarEntry(index.toFloat(), chartData.value))
//        }
//
//        //?????? ????????? ?????? ??????
//        val barDataSet = BarDataSet(values, "").apply {
//            setDrawValues(false)
//
//            val colors = ArrayList<Int>()
//            colors.add(Color.argb(100,51, 155, 255))
//            setColors(colors)
//            highLightAlpha = 0
//        }
//
//        //?????? ????????? ?????? ??????
//        val dataSets = mutableListOf(barDataSet)
//        val data = BarData(dataSets as List<IBarDataSet>?).apply {
////            setValueTextSize(30F)
//            barWidth = 0.3F
//        }
//        //??????????????? ?????? 0.1???
//        with(binding.weekBarChart) {
//            animateY(100)
//            xAxis.apply {
//                position = XAxis.XAxisPosition.BOTTOM
//                setDrawGridLines(false) //??? ??????
//                textColor = whiteColor
//                //??? ~ ???
//                valueFormatter = object : ValueFormatter() {
//                    override fun getFormattedValue(value: Float): String {
//                        return barData[value.toInt()].date
//                    }
//                }
//            }
//
////            axisLeft.labelPosition = true
////            axisLeft.labelCount =
////            axisLeft.setPosition(floatArrayOf(0f, 10f, 20f, 50f, 100f, 300f))
//            //?????? ?????? ???, Y?????? ( ?????? ?????????,????????? )
//            axisRight.apply {
//                textColor = whiteColor
//                setDrawAxisLine(false) //??????
//                gridColor = transparentBlackColor
//                gridLineWidth = 0.5F
//                enableGridDashedLine(5f,5f,5f)
//
////                axisMaximum = 24F //?????????
////                granularity  = 6F //30???????????? ?????? ???????????? granularity ????????? ??? ?????????
////                setLabelCount(6,true) //??? ????????????
////                axisMinimum = 3F //?????????
//                //y??? ?????? ?????????
//                valueFormatter = object : ValueFormatter() {
//                    private val mFormat: DecimalFormat = DecimalFormat("###")
//                    override fun getFormattedValue(value: Float): String {
//                        return mFormat.format(value) + "???"
//                    }
//                }
//            }
//
//            //?????? ????????? ???, Y?????? false??????
//            axisLeft.apply {
//                isEnabled = false
//                gridColor = transparentBlackColor
//                //??????????????? ????????? ?????? ??? ???
////                var chartDataMax = listData.maxBy { it -> it.value}
////                barData.forEachIndexed { index, chartData ->
////                    for (i in chartData.value) {
//////                        var chartDataMax = listData.maxBy { it -> it. }
////                        var maxValue = i
////                        Log.d("aaa", "$maxValue")
////                        while (i > axisMaximum) {
////                            count++
////                            if (i > axisMaximum) {
////                                axisMaximum = maxValue
////                            } else {
////                                axisMaximum = 9F
////                            }
////                        }
////                    }
////                }
//                setLabelCount(6,true) //??? ????????????
//                axisMaximum = 24F //?????????
//                granularity  = 6F //30???????????? ?????? ???????????? granularity ????????? ??? ?????????
//                axisMinimum = 3F //?????????
////
//
//            }
//            notifyDataSetChanged()
//            this.data = data
//            invalidate()
//        }
//    }
//
//
//    private fun moveCalendarByWeek(monDay:TextView,sunDay:TextView,rBtn:ImageButton,lBtn:ImageButton){
//        val calendarWeekNow : LocalDateTime = LocalDateTime.now()
//        val monday : LocalDateTime = LocalDateTime.now().with(DayOfWeek.MONDAY)//?????? ????????? ?????????
//        val sunday : LocalDateTime = LocalDateTime.now().with(DayOfWeek.SUNDAY)
//
//        val textformatter : DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
//        var count: Int = 0
//
//        monDay.text = monday.format(textformatter)
//        sunDay.text = sunday.format(textformatter)
//
//        rBtn.setOnClickListener{
//            count++
//            val mondayValue : LocalDateTime = monday.plusWeeks(count.toLong())
//            monDay.text = mondayValue.format(textformatter).toString()
//            val sundayValue : LocalDateTime = sunday.plusWeeks(count.toLong())
//            sunDay.text = sundayValue.format(textformatter).toString()
//        }
//
//        lBtn.setOnClickListener{
//            count--
//            val sundayValue : LocalDateTime = sunday.plusWeeks(count.toLong())
//            sunDay.text = sundayValue.format(textformatter).toString()
//            val mondayValue : LocalDateTime = monday.plusWeeks(count.toLong())
//            monDay.text = mondayValue.format(textformatter).toString()
//        }
//
//    }
//
//    private fun weeklySubjectPieChart(pieChart : PieChart, list: MutableList<Subjects>){
//        pieChart.setUsePercentValues(true)
//        Log.d("qwqwqwqwqw",subjects.studytime.toString())
//        Log.d("qwqwqwqwqw",subjects.color.toString())
//        Log.d("aqaqAllList", list.size.toString())
//
//        val values = mutableListOf<PieEntry>()
//        val colorItems = mutableListOf<Int>()
//        list.forEachIndexed{ index, _ ->
//            values.add(PieEntry(list[index].studytime.toFloat(), list[index].name))
//            colorItems.add(index,Color.parseColor(list[index].color))
//        }
//
//        val pieDataSet = PieDataSet(values,"")
//        pieDataSet.colors = colorItems
//        pieDataSet.apply {
////            valueTextColor = Color.BLACK
//            setDrawValues(false) //????????? ???????????? ??? ?????????
//            valueTextSize = 16f
//        }
//        //% : ????????? ?????? ????????? ????????? ??????
//        val pieData = PieData(pieDataSet)
//        pieChart.apply {
//            data = pieData
//            description.isEnabled = false //?????? ????????? ????????? ?????? ???????????? ????????? ????????????.
//            isRotationEnabled = false //???????????? ??????????????? ?????? ??? ??????
////            centerText = "this is color" //????????? ??? ????????? ????????? ?????????
////            setEntryLabelColor(Color.RED) //????????? ???????????? ????????? ??? ??????
//            isEnabled = false
//            legend.isEnabled = false //?????? ?????????
//            isDrawHoleEnabled = true //????????? ?????? ????????? ??????
//            holeRadius = 50f //????????? ????????? ??? ?????????
//            setDrawEntryLabels(false) //????????? ?????? ?????? ??????
//            animateY(1400, Easing.EaseInOutQuad)
//            animate()
//        }
//
//    }
//
//    private fun requestPermission(): Boolean {
//        var permissions = false
//        TedPermission.with(context)
//            .setPermissionListener(object : PermissionListener {
//                override fun onPermissionGranted() {
//                    permissions = true      //p0=response(??????)
//                    val shareIntent = Intent(context, ShareActivity::class.java)
//                    startActivity(shareIntent)
////                    finish()
//                }
//
//                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
//                    permissions = false
//                }
//
//            })
//            .setDeniedMessage("?????? ??????????????? ????????? ????????????????????????.")
//            .setPermissions(Manifest.permission.CAMERA)
//            .check()
//        return permissions
//    }
//
//
//}