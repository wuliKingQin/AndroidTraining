package com.wuliqinwang.patch

import org.apache.commons.codec.digest.DigestUtils
import java.io.*
import java.util.*

// DES: 包工具类
object PatchUtils {

    // DES: 将正常的字母变成首字母大写
    fun getCapitalLetter(content: String?): String? {
        content ?: return ""
        return content[0].toUpperCase().plus(content.substring(1))
    }

    // DES: 字节数组的md5值
    fun toMd5Hex(byteCode: ByteArray?): String {
        try {
            return DigestUtils.md5Hex(byteCode)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    // DES: 从文件中读取保存的Md5值
    fun readMd5HexFromFile(hexFile: File?): Map<String, String>{
        val md5Map = HashMap<String, String>()
        hexFile ?: return md5Map
        BufferedReader(InputStreamReader(FileInputStream(hexFile))).use { br ->
            var line: String?
            while (br.readLine().also { line = it } != null) {
                line?.split(":")?.apply {
                    if(size >= 2) {
                        md5Map[get(0)] = get(1)
                    }
                }
            }
        }
        return md5Map
    }

    // DES: 写入Md5值到文件
    fun writeMd5HexToFile(
        md5HexMap: Map<String, String?>,
        md5HexFile: File
    ) {
        FileOutputStream(md5HexFile).use {
            md5HexMap.forEach { (key, value) ->
                it.write("$key:$value\n".toByteArray())
            }
        }
    }
}