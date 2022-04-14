package com.wuliqinwang.android.anr.monitor.cache

import android.os.Parcel
import android.os.Parcelable

// 用于记录消息调度的实体类
data class Record(
    // 记录消息的唯一Id
    var id: Int = 0,
    // 消息类型
    var type: Int = 0,
    // 消息对应的类型
    var what: Int = 0,
    // 消息对应的处理器，该值是最后一个消息Handler
    var handler: String? = null,
    // 消息调度耗时，该值可能是几个消息调度耗时的之和
    var wall: Long = 0,
    // 消息被阻塞的时长
    var block: Long = 0,
    // cpu使用率
    var cpu: Long = 0,
    // 记录消息调度个数
    var count: Int = 0,
    // 用于存放有超时的时候获取到主线程的堆栈信息
    var stackInfo: String? = null
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(type)
        parcel.writeInt(what)
        parcel.writeString(handler)
        parcel.writeLong(wall)
        parcel.writeLong(block)
        parcel.writeLong(cpu)
        parcel.writeInt(count)
        parcel.writeString(stackInfo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Record> {
        override fun createFromParcel(parcel: Parcel): Record {
            return Record(parcel)
        }

        override fun newArray(size: Int): Array<Record?> {
            return arrayOfNulls(size)
        }
    }
}
