package com.wuliqinwang.android.anr.monitor.cache

import android.util.SparseArray
import androidx.core.util.*

// 实现LRU缓存记录器
class LruRecorder(
    private val capacity: Int = MAX_RECORD_COUNT
) : MutableMap<Int, Record?> {

    companion object {
        // 最大记录大小是100
        private const val MAX_RECORD_COUNT = 100
    }

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

    override val size: Int = mCacheMap.size()

    override val values: MutableCollection<Record?>
        get() = mCacheMap.valueIterator().asSequence().toHashSet()

    override fun containsKey(key: Int): Boolean {
        return mCacheMap.containsKey(key)
    }

    override fun containsValue(value: Record?): Boolean {
        value ?: return false
        return mCacheMap.indexOfKey(value.id) >= 0
    }

    override fun get(key: Int): Record? {
        val tempValue = mCacheMap.get(key) ?: return null

        return tempValue.value
    }

    override fun isEmpty(): Boolean {
        return mCacheMap.isEmpty()
    }

    override fun clear() {
        mCacheMap.clear()
    }

    override fun put(key: Int, value: Record?): Record? {
        mCacheMap.put(key, RecordEntry(key, value))
        return value
    }

    override fun putAll(from: Map<out Int, Record?>) {
        for (entry in from) {
            mCacheMap.put(entry.key, RecordEntry(entry.key, entry.value))
        }
    }

    override fun remove(key: Int): Record? {
        val tempValue = mCacheMap.get(key)
        mCacheMap.remove(key)
        return tempValue.value
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