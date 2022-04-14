package com.wuliqinwang.android.anr

class TestField {
    companion object {
        private var mVersion = 10
    }

    private var mRunnable = Runnable {
        println("============Runnable=============")
    }
}

class TestMethod {
    companion object {

        private fun test() {
            println("============test=============")
        }

        @JvmStatic
        private fun executeTask() {
            println("============executeTask=============")
        }

        @JvmStatic
        private fun executeTask(message: String?) {
            println("============executeTask==message=${message}===========")
        }
    }

    private fun print() {
        println("============print=============")
    }

    private fun print(message: String?) {
        println("============print=message=${message}============")
    }
}