package com.wuliqinwang.android.anr

import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.wuliqinwang.android.BR
import com.wuliqinwang.android.R
import com.wuliqinwang.android.anr.monitor.cache.Record

// 记录数据适配器
class RecordDataAdapter(
    private var viewModel: ViewModel
): AbstractDataAdapter<Record>(){

    companion object {
        private const val DEFAULT_TIME_SIZE = 128

        @JvmStatic
        @BindingAdapter("showType", "recordInfo")
        fun setRecordInfo(targetView: TextView, showType: Int, record: Record) {
            targetView.text = when(showType) {
                0 -> "type: ${record.type}"
                1 -> "wall: ${record.wall}毫秒"
                2 -> "count: ${record.count}"
                3 -> "what: ${record.what}"
                4 -> "handler: ${record.handler}"
                else -> ""
            }
        }
    }

    override val dataList: DataOperator<Record> by lazy {
        DataOperator<Record>(this, ArrayList(DEFAULT_TIME_SIZE))
    }

    override fun getItemLayoutId(viewType: Int): Int {
        return R.layout.rv_record_item
    }

    override fun ViewDataBinding.onDataBinding(data: Record) {
        setVariable(BR.anrVm, viewModel)
        setVariable(BR.record, data)
    }
}