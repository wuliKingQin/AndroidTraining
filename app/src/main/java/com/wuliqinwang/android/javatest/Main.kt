package com.wuliqinwang.android.javatest

class TestRunnable: Runnable {
    override fun run() {
    }
}

class Thread1: Thread(){

    var ss = "000000000000000000"

    override fun run() {
        while (!isInterrupted) {
            println("===============")
        }
    }

}

fun main() {
    val thread = Thread1()
    thread.start()
    Thread.sleep(20)
    thread.interrupt()
}