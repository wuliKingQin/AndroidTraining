package com.wuliqinwang.android.convert

import com.swallowsonny.convertextlibrary.readFloatLE

fun main() {
    val float = 2.5f
    float.toBits()
    println(byteArrayOf(64,25,-103,-102).readFloatLE())
}