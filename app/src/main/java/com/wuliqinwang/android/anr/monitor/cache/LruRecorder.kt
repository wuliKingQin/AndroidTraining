package com.wuliqinwang.android.anr.monitor.cache

import android.util.SparseArray
import androidx.core.util.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

// 实现LRU缓存记录器
class LruRecorder private constructor(
    private val capacity: Int = MAX_RECORD_COUNT
) : MutableMap<Int, Record?> {

    companion object {

        // 最大记录大小是100
        private const val MAX_RECORD_COUNT = 100

        // 保存一份全局单列
        private val mLruRecorder by lazy {
            Collections.synchronizedMap(LruRecorder())
        }

        // 获取记录
        @JvmStatic
        fun getRecord(key: Int): Record? {
            return mLruRecorder[key]
        }

        // 添加记录
        @JvmStatic
        fun putRecord(record: Record) {
            mLruRecorder[record.id] = record
        }

        // 获取记录器里面的所有的值
        @JvmStatic
        fun getAllRecords(): MutableCollection<Record?> {
            return mLruRecorder.values
        }

        // 清除所有的记录信息
        @JvmStatic
        fun clearAll() {
            mLruRecorder.clear()
        }

        // 得到记录器大小
        @JvmStatic
        fun getRecordSize(): Int = mLruRecorder.size
    }

    // 当前元素个数
    private var mSize = 0

    private var head = RecordEntry()

    private var tail = RecordEntry()

    private val mCacheMap = SparseArray<RecordEntry>(capacity)

    init {
        head.next = tail
        tail.prev = head
    }

    override val entries: MutableSet<MutableMap.MutableEntry<Int, Record?>>
        get(): MutableSet<MutableMap.MutableEntry<Int, Record?>> {
            val hashSet = HashSet<MutableMap.MutableEntry<Int, Record?>>()
            mCacheMap.forEach { key, value ->
                hashSet.add(RecordEntry(key, value.value))
            }
            return hashSet
        }

    override val keys: MutableSet<Int>
        get() = mCacheMap.keyIterator().asSequence().toHashSet()

    override val size: Int
        get() = mSize

    override val values: MutableCollection<Record?>
        get() {
            val tempValues = ArrayList<Record?>(size)
            mCacheMap.valueIterator().forEach {
                tempValues.add(it.value)
            }
            return tempValues
        }

    override fun containsKey(key: Int): Boolean {
        return mCacheMap.containsKey(key)
    }

    override fun containsValue(value: Record?): Boolean {
        value ?: return false
        return mCacheMap.indexOfKey(value.id) >= 0
    }

    override fun get(key: Int): Record? {
        val tempValue = mCacheMap.get(key) ?: return null
        moveToHead(tempValue)
        return tempValue.value
    }

    override fun isEmpty(): Boolean {
        return mCacheMap.isEmpty()
    }

    override fun clear() {
        mSize = 0
        head = RecordEntry()
        tail = RecordEntry()
        head.next = tail
        tail.prev = head
        mCacheMap.clear()
    }

    override fun put(key: Int, value: Record?): Record? {
        var tempNode = mCacheMap[key]
        if (tempNode == null) {
            tempNode = RecordEntry(key, value)
            mCacheMap.put(key, tempNode)
            addToHead(tempNode)
            mSize ++
            if (size > capacity) {
                val tailNode = removeTail()
                if (tailNode != null) {
                    mCacheMap.remove(tailNode.key)
                }
                mSize --
            }
        } else {
            tempNode.value = value
            moveToHead(tempNode)
        }
        return value
    }

    override fun putAll(from: Map<out Int, Record?>) {
        for (entry in from) {
            put(entry.key, entry.value)
        }
    }

    override fun remove(key: Int): Record? {
        val tempNode = mCacheMap[key] ?: return null
        removeNode(tempNode)
        mCacheMap.remove(tempNode.key)
        mSize --
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