package com.example.haojizhang.ui.charts

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

data class PieSlice(
    val label: String,
    val value: Float
)

@Composable
fun CategoryPieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            PieChart(ctx).apply {
                description.isEnabled = false
                setUsePercentValues(true)
                isDrawHoleEnabled = true
                holeRadius = 55f
                transparentCircleRadius = 60f
                setEntryLabelColor(Color.DKGRAY)
                setEntryLabelTextSize(12f)
                legend.isEnabled = true
                setNoDataText("暂无数据")
            }
        },
        update = { chart ->
            if (slices.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val entries = slices.map { PieEntry(it.value, it.label) }
            val dataSet = PieDataSet(entries, "分类占比").apply {
                // 不指定颜色也能跑，但 MPAndroidChart 默认颜色偏少，这里给一组常用颜色
                setColors(
                    intArrayOf(
                        Color.parseColor("#4E79A7"),
                        Color.parseColor("#F28E2B"),
                        Color.parseColor("#E15759"),
                        Color.parseColor("#76B7B2"),
                        Color.parseColor("#59A14F"),
                        Color.parseColor("#EDC949"),
                        Color.parseColor("#AF7AA1"),
                        Color.parseColor("#FF9DA7")
                    ), 255
                )
                sliceSpace = 2f
                valueTextSize = 12f
                valueTextColor = Color.WHITE
            }

            val data = PieData(dataSet).apply {
                setValueFormatter(PercentFormatter(chart))
            }

            chart.data = data
            chart.invalidate()
        }
    )
}
