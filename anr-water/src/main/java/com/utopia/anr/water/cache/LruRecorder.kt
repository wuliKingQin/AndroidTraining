package com.utopia.anr.water.cache

import android.util.SparseArray
import androidx.core.util.*
import kotlin.collections.ArrayList

// 实现LRU缓存记录器
class LruRecorder private constructor(
    private val capacity: Int = MAX_RECORD_COUNT
){

    companion object {

        // 最大记录大小是100
        private const val MAX_RECORD_COUNT = 100

        // 保存一份全局单列
        private val mLruRecorder by lazy {
            LruRecorder()
        }

        // 获取记录
        @JvmStatic
        fun getRecord(key: Int): Record? {
            synchronized(mLruRecorder) {
                return mLruRecorder.get(key)
            }
        }

        // 添加记录
        @JvmStatic
        fun putRecord(record: Record): Record {
            synchronized(mLruRecorder) {
                mLruRecorder.put(record.id, record)
            }
            return record
        }

        // 获取记录器里面的所有的值
        @JvmStatic
        fun getAllRecords(): List<Record> {
            synchronized(mLruRecorder) {
                val tempList = ArrayList<Record>(getRecordSize())
                mLruRecorder.mCacheMap.forEach { _, value ->
                    value.value?.apply {
                        tempList.add(this)
                    }
                }
                return tempList
            }
        }

        // 清除所有的记录信息
        @JvmStatic
        fun clearAll() {
            synchronized(mLruRecorder) {
                mLruRecorder.clear()
            }
        }

        // 得到记录器大小
        @JvmStatic
        fun getRecordSize(): Int = synchronized(mLruRecorder) {
            mLruRecorder.size
        }
    }

    // 当前元素个数
    private var size = 0

    private var head = RecordEntry()

    private var tail = RecordEntry()

    private val mCacheMap = SparseArray<RecordEntry>(capacity)

    init {
        head.next = tail
        tail.prev = head
    }

    private var values = mCacheMap.valueIterator()

    fun containsKey(key: Int): Boolean {
        return mCacheMap.containsKey(key)
    }

    fun containsValue(value: Record?): Boolean {
        value ?: return false
        return mCacheMap.indexOfKey(value.id) >= 0
    }

    fun get(key: Int): Record? {
        val tempValue = mCacheMap.get(key) ?: return null
        moveToHead(tempValue)
        return tempValue.value
    }

    fun isEmpty(): Boolean {
        return mCacheMap.isEmpty()
    }

    fun clear() {
        size = 0
        head = RecordEntry()
        tail = RecordEntry()
        head.next = tail
        tail.prev = head
        mCacheMap.clear()
    }

    fun put(key: Int, value: Record?): Record? {
        var tempNode = mCacheMap[key]
        if (tempNode == null) {
            tempNode = RecordEntry(key, value)
            mCacheMap.put(key, tempNode)
            addToHead(tempNode)
            size ++
            if (size > capacity) {
                val tailNode = removeTail()
                if (tailNode != null) {
                    mCacheMap.remove(tailNode.key)
                }
                size --
            }
        } else {
            tempNode.value = value
            moveToHead(tempNode)
        }
        return value
    }

    fun putAll(from: Map<out Int, Record?>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    fun remove(key: Int): Record? {
        val tempNode = mCacheMap[key] ?: return null
        removeNode(tempNode)
        mCacheMap.remove(tempNode.key)
        size --
        return tempNode.value
    }

    // 将节点添加到头节点
    private fun addToHead(node: RecordEntry?) {
        node ?: return
        node.prev = head
        node.next = head.next
        head.next?.prev = node
        head.next = node
    }

    // 移除节点
    private fun removeNode(node: RecordEntry?) {
        node ?: return
        node.prev?.next = node.next
        node.next?.prev = node.prev
    }

    // 将节点移动到头节点
    private fun moveToHead(node: RecordEntry?) {
        removeNode(node)
        addToHead(node)
    }

    // 移除尾节点
    private fun removeTail(): RecordEntry? {
        val tailNode = tail.prev
        removeNode(tailNode)
        return tailNode
    }

    // 内部实现的记录实例
    private data class RecordEntry(
        override var key: Int = 0,
        override var value: Record? = null
    ) : MutableMap.MutableEntry<Int, Record?> {

        var prev: RecordEntry? = null
        var next: RecordEntry? = null

        override fun setValue(newValue: Record?): Record? {
            val tempValue = value
            value = newValue
            return tempValue
        }
    }
}