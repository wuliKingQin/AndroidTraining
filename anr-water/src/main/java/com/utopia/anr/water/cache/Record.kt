package com.utopia.anr.water.cache

import android.os.Parcel
import android.os.Parcelable
import com.utopia.anr.water.dispatchers.Recorder

// 用于记录消息调度的实体类
data class Record(
    // 记录消息的唯一Id
    var id: Int = 0,
    // 消息类型
    var type: Int = 0,
    // 记录描述，默认显示"普通记录"
    var des: String? = "普通记录",
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

    constructor(builder: Builder): this(
        builder.id,
        builder.type,
        builder.des,
        builder.what,
        builder.handler,
        builder.wall,
        builder.block,
        builder.cpu,
        builder.count,
        builder.stackInfo
    )

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readInt(),
        parcel.readString()
    )

    // 新建一个Builder类
    fun newBuilder(): Builder {
        return Builder(this)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(type)
        parcel.writeString(des)
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

    // 记录构建器
    class Builder(record: Record){
        // 记录消息的唯一Id
        internal var id = record.id
        // 消息类型
        internal var type = record.type
        // 记录描述，默认显示"普通记录"
        internal var des = record.des
        // 消息对应的类型
        internal var what = record.what
        // 消息对应的处理器，该值是最后一个消息Handler
        internal var handler = record.handler
        // 消息调度耗时，该值可能是几个消息调度耗时的之和
        internal var wall = record.wall
        // 消息被阻塞的时长
        internal var block = record.block
        // cpu使用率
        internal var cpu = record.cpu
        // 记录消息调度个数
        internal var count = record.count
        // 用于存放有超时的时候获取到主线程的堆栈信息
        internal var stackInfo = record.stackInfo

        fun setId(id: Int): Builder {
            this.id = id
            return this
        }

        fun setType(type: Int): Builder {
            this.type = type
            return this
        }

        fun setStackInfo(stackInfo: String?): Builder {
            this.stackInfo = stackInfo
            return this
        }

        fun setWhat(what: Int): Builder {
            this.what = what
            return this
        }

        fun setDes(des: String): Builder {
            this.des = des
            return this
        }

        fun setHandler(handler: String?): Builder {
            this.handler = handler
            return this
        }

        fun setWall(wall: Long): Builder {
            this.wall = wall
            return this
        }

        fun setBlock(block: Long): Builder {
            this.block = block
            return this
        }

        fun setCpu(cpu: Long): Builder {
            this.cpu = cpu
            return this
        }

        fun setCount(count: Int): Builder {
            this.count = count
            return this
        }

        @JvmOverloads
        fun build(isSaveRecord: Boolean = true): Record {
            return Record(this).apply {
                if (isSaveRecord) {
                    LruRecorder.putRecord(this)
                }
            }
        }
    }
}
