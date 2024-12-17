package com.example.smartstudy.data.local

import androidx.room.TypeConverter

// ColorListConverter 用于处理颜色列表（List<Int>）与字符串（String）之间的转换
// 它是一个类型转换器，Room 会使用它来在数据库中存储 List<Int> 类型的字段，
// 同时在读取时将字符串转换回 List<Int> 类型
class ColorListConverter {

    @TypeConverter
    fun fromColorList(colorList: List<Int>): String {
        return colorList.joinToString(",") { it.toString() }
    }

    @TypeConverter
    fun toColorList(colorListString: String): List<Int> {
        return colorListString.split(",").map { it.toInt() }
    }
}