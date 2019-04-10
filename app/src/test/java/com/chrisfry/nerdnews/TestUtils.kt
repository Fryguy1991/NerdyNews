package com.chrisfry.nerdnews

import com.chrisfry.nerdnews.utils.LogUtils
import java.io.*

class TestUtils {
    companion object {
        private val TAG = TestUtils::class.java.name

        /**
         * Function that attempts to read a json file based on file name
         *
         * @param filename: String representation of the requested file name
         */
        @Throws(IOException::class)
        fun readJsonFile(filename: String): String {
            var rootPath = File("test.txt").absolutePath
            if (rootPath == null) {
                LogUtils.error(TAG, "Could not find path for file")
                return AppConstants.EMPTY_STRING
            } else {
                rootPath = rootPath.replace("\\test.txt", AppConstants.EMPTY_STRING)
                val br = BufferedReader(InputStreamReader(FileInputStream("$rootPath\\src\\test\\java\\com\\chrisfry\\nerdnews\\demoData\\$filename")))
                val sb = StringBuilder()
                var line = br.readLine()
                while (line != null) {
                    sb.append(line)
                    line = br.readLine()
                }

                return sb.toString()
            }
        }
    }
}