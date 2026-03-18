package com.peimama.renzi.data.seed

import android.content.Context
import kotlin.math.ceil
import kotlinx.serialization.json.Json

class SeedDataLoader(
    private val context: Context,
) {
    private val parser = Json {
        ignoreUnknownKeys = true
    }

    fun load(): SeedRoot {
        val json = context.assets.open("sample_words.json")
            .bufferedReader()
            .use { it.readText() }
            .removePrefix("\uFEFF")
            .trim()
        return parser.decodeFromString<SeedRoot>(json).normalizeForMvp()
    }

    private fun SeedRoot.normalizeForMvp(): SeedRoot {
        val normalizedScenes = scenes.mapIndexed { index, scene ->
            val sceneOrder = scene.sortOrder.takeIf { it > 0 } ?: (index + 1)
            if (scene.id == "core3000") {
                normalizeCoreScene(scene, sceneOrder)
            } else {
                normalizeLifeScene(scene, sceneOrder)
            }
        }.sortedBy { it.sortOrder }

        return copy(scenes = normalizedScenes)
    }

    private fun normalizeCoreScene(scene: SeedScene, sortOrder: Int): SeedScene {
        val normalizedLessons = scene.lessons.mapIndexed { lessonIndex, lesson ->
            val normalizedWords = lesson.words.map { word ->
                val text = normalizeWordText(word)
                val pinyin = word.pinyin.repairOrDefault("")
                val meaning = word.meaning.repairOrDefault("常用字：$text")
                val example = word.exampleSentence.repairOrDefault("请认读：$text。")
                word.copy(
                    text = text,
                    pinyin = pinyin,
                    meaning = meaning,
                    exampleSentence = example,
                    difficulty = word.difficulty.coerceIn(1, 3),
                )
            }

            val focusWords = normalizedWords.take(3).joinToString("、") { it.text }
            val safeSortOrder = lesson.sortOrder.takeIf { it > 0 } ?: (lessonIndex + 1)
            val defaultTitle = "第${safeSortOrder}课：$focusWords"
            val defaultDesc = "常用字分组练习"

            lesson.copy(
                title = lesson.title.repairOrDefault(defaultTitle),
                description = lesson.description.repairOrDefault(defaultDesc),
                sortOrder = safeSortOrder,
                words = normalizedWords,
            )
        }

        return scene.copy(
            name = scene.name.repairOrDefault("3000日常常用字"),
            description = scene.description.repairOrDefault("按课练习，覆盖日常阅读高频字。"),
            sortOrder = sortOrder,
            lessons = normalizedLessons,
        )
    }

    private fun normalizeLifeScene(scene: SeedScene, sortOrder: Int): SeedScene {
        val template = sceneTemplates[scene.id] ?: SceneTemplate(
            name = "生活识字",
            description = "围绕生活场景学习常用词",
            words = commonDailyWords,
        )

        val lessonCount = scene.lessons.size.coerceAtLeast(2)
        val sourceLessonIds = scene.lessons.map { it.id }
        val sourceWordIds = scene.lessons.flatMap { lesson -> lesson.words.map { it.id } }
        val targetWordCount = if (lessonCount >= 8) 52 else 48
        val wordBank = buildWordBank(template.words, targetWordCount)
        val wordsPerLesson = ceil(wordBank.size.toDouble() / lessonCount.toDouble()).toInt().coerceAtLeast(4)
        var globalWordIndex = 0

        val normalizedLessons = (0 until lessonCount).map { lessonIndex ->
            val start = lessonIndex * wordsPerLesson
            val end = (start + wordsPerLesson).coerceAtMost(wordBank.size)
            val lessonWords = if (start < end) {
                wordBank.subList(start, end)
            } else {
                wordBank.take(wordsPerLesson)
            }
            val focusWords = lessonWords.take(3).joinToString("、")
            val title = template.lessonTitles.getOrNull(lessonIndex) ?: "第${lessonIndex + 1}课：$focusWords"
            val wordItems = lessonWords.mapIndexed { localIndex, text ->
                val sourceWordId = sourceWordIds.getOrNull(globalWordIndex)
                globalWordIndex += 1
                val wordId = if (sourceWordId.isNullOrBlank()) {
                    "w_${scene.id}_l${lessonIndex + 1}_${localIndex + 1}"
                } else {
                    sourceWordId
                }
                SeedWord(
                    id = wordId,
                    text = text,
                    pinyin = "",
                    meaning = "生活常用词：$text",
                    exampleSentence = "在生活里认一认“$text”。",
                    difficulty = if (text.length >= 3) 2 else 1,
                )
            }

            SeedLesson(
                id = sourceLessonIds.getOrNull(lessonIndex).takeUnless { it.isNullOrBlank() }
                    ?: "${scene.id}_l${lessonIndex + 1}",
                title = title,
                description = "围绕${template.name}的常用词练习",
                sortOrder = lessonIndex + 1,
                words = wordItems,
            )
        }

        return scene.copy(
            name = template.name,
            description = template.description,
            sortOrder = sortOrder,
            lessons = normalizedLessons,
        )
    }

    private fun normalizeWordText(word: SeedWord): String {
        if (!word.text.needsRepair()) return word.text.trim()

        extractLastHanChunk(word.exampleSentence)?.let { return it }
        extractLastHanChunk(word.meaning)?.let { return it }

        val idToken = word.id.substringAfterLast('_')
        return when {
            idToken.all(Char::isDigit) -> "字$idToken"
            idToken.isBlank() -> "词"
            else -> idToken.lowercase()
        }
    }

    private fun extractLastHanChunk(source: String): String? {
        if (source.isBlank()) return null
        return hanChunkRegex.findAll(source)
            .map { it.value.trim() }
            .filter { it.isNotBlank() }
            .lastOrNull()
    }

    private fun buildWordBank(primary: List<String>, targetCount: Int): List<String> {
        val unique = linkedSetOf<String>()
        (primary + commonDailyWords).forEach { raw ->
            val token = raw.trim()
            if (token.isNotBlank()) unique += token
        }
        while (unique.size < targetCount) {
            unique += "词${unique.size + 1}"
        }
        return unique.take(targetCount)
    }

    private fun String.repairOrDefault(default: String): String {
        val value = trim()
        if (value.needsRepair()) return default
        return value
    }

    private fun String.needsRepair(): Boolean {
        if (isBlank()) return true
        return contains('?') || contains('\uFFFD')
    }

    private data class SceneTemplate(
        val name: String,
        val description: String,
        val words: List<String>,
        val lessonTitles: List<String> = emptyList(),
    )

    private val hanChunkRegex = Regex("[\\u4E00-\\u9FFF]{1,6}")

    private val commonDailyWords = listOf(
        "今天", "明天", "昨天", "现在", "早上", "中午", "晚上", "时间",
        "日期", "星期", "可以", "不要", "需要", "这里", "那里", "前面",
        "后面", "左边", "右边", "上面", "下面", "里面", "外面", "开始",
        "结束", "打开", "关闭", "确认", "取消", "返回", "下一步", "上一页",
        "注意", "安全", "帮助", "谢谢", "请问", "好的", "慢慢来", "休息",
        "喝水", "吃饭", "睡觉", "电话", "地址", "姓名", "密码", "金额",
        "数字", "扫码", "支付", "排队", "完成", "成功", "提醒", "学习",
    )

    private val sceneTemplates = mapOf(
        "home" to SceneTemplate(
            name = "家里识字",
            description = "厨房和家庭常见字词",
            words = listOf(
                "米", "面", "油", "盐", "水", "锅", "碗", "筷子",
                "勺子", "盘子", "刀", "菜板", "冰箱", "灶台", "开火", "关火",
                "洗菜", "切菜", "炒菜", "煮饭", "米饭", "面条", "粥", "汤",
                "鸡蛋", "牛奶", "门", "窗", "客厅", "卧室", "厕所", "阳台",
                "开关", "电灯", "插座", "洗碗", "拖地", "扫地", "垃圾", "收纳",
            ),
        ),
        "market" to SceneTemplate(
            name = "买菜识字",
            description = "买菜和结账常见字词",
            words = listOf(
                "菜", "肉", "钱", "鱼", "蛋", "虾", "豆腐", "白菜",
                "青菜", "土豆", "西红柿", "黄瓜", "茄子", "辣椒", "葱", "姜",
                "蒜", "水果", "苹果", "香蕉", "橙子", "葡萄", "买", "卖",
                "斤", "两", "包", "便宜", "新鲜", "贵", "元", "角",
                "分", "现金", "扫码", "支付", "收银台", "小票", "排队", "袋子",
            ),
        ),
        "traffic" to SceneTemplate(
            name = "外出识字",
            description = "地铁公交出行常见字词",
            words = listOf(
                "路", "站", "车", "地铁", "公交", "火车", "高铁", "入口",
                "出口", "上车", "下车", "换乘", "候车", "车票", "站台", "下一站",
                "终点", "起点", "红绿灯", "斑马线", "左转", "右转", "直行", "打车",
                "司机", "座位", "扶手", "安全带", "地图", "导航", "东", "西",
                "南", "北", "步行", "停车", "过街", "天桥", "地下通道", "到达",
            ),
        ),
        "hospital" to SceneTemplate(
            name = "医院识字",
            description = "看病就医常见字词",
            words = listOf(
                "医院", "门诊", "挂号", "缴费", "药房", "检查", "医生", "护士",
                "病历", "处方", "化验", "抽血", "B超", "CT", "体温", "血压",
                "复诊", "住院", "出院", "急诊", "饭前", "饭后", "一天三次", "一日两次",
                "早晚各一次", "按时", "用药", "过敏", "慎用", "复查", "等待", "叫号",
                "窗口", "报告", "诊室", "取药", "说明书", "剂量", "疼痛", "咳嗽",
            ),
        ),
        "phone" to SceneTemplate(
            name = "手机识字",
            description = "手机基础操作常见字词",
            words = listOf(
                "电话", "微信", "视频", "语音", "接听", "挂断", "返回", "确定",
                "取消", "联系人", "拨号", "通话", "免提", "音量", "铃声", "短信",
                "相册", "拍照", "录像", "手电筒", "闹钟", "日历", "天气", "设置",
                "网络", "蓝牙", "电量", "充电", "锁屏", "解锁", "支付", "打车",
                "地图", "扫一扫", "收款", "付款", "红包", "转账", "小程序", "更新",
            ),
        ),
        "bank" to SceneTemplate(
            name = "银行识字",
            description = "银行办事常见字词",
            words = listOf(
                "银行", "取号", "柜台", "窗口", "大厅", "叫号", "排队", "存钱",
                "取钱", "转账", "汇款", "查询", "余额", "明细", "卡号", "密码",
                "身份证", "签名", "现金", "零钱", "手续费", "网点", "营业时间", "ATM",
                "自助机", "存折", "银行卡", "开户", "销户", "挂失", "补卡", "短信提醒",
            ),
        ),
        "community" to SceneTemplate(
            name = "社区识字",
            description = "小区生活常见字词",
            words = listOf(
                "小区", "楼栋", "单元", "门禁", "电梯", "楼层", "物业", "保安",
                "快递", "驿站", "停车", "车位", "公告", "停水", "停电", "维修",
                "报修", "社区", "居委会", "活动", "广场", "垃圾", "分类", "厨余",
                "可回收", "有害", "其他", "访客", "登记", "出入", "门口", "地下车库",
            ),
        ),
        "safety" to SceneTemplate(
            name = "防诈骗识字",
            description = "识别风险和防骗常见字词",
            words = listOf(
                "验证码", "链接", "陌生", "电话", "短信", "中奖", "转账", "汇款",
                "密码", "账号", "支付", "退款", "客服", "官方", "警察", "报警",
                "110", "诈骗", "骗子", "隐私", "身份证", "银行卡", "二维码", "风险",
                "提醒", "核实", "不要点", "不要信", "挂断", "求助", "家人", "朋友",
            ),
        ),
        "supermarket" to SceneTemplate(
            name = "超市识字",
            description = "超市购物常见字词",
            words = listOf(
                "超市", "推车", "购物篮", "货架", "标签", "折扣", "促销", "会员",
                "价格", "条码", "扫码", "称重", "散装", "冷藏", "冷冻", "蔬菜",
                "水果", "饮料", "牛奶", "面包", "调料", "纸巾", "洗衣液", "收银台",
                "小票", "袋子", "排队", "结账", "优惠", "满减", "积分", "日期",
            ),
        ),
        "restaurant" to SceneTemplate(
            name = "餐馆识字",
            description = "点餐用餐常见字词",
            words = listOf(
                "餐馆", "菜单", "点菜", "米饭", "面条", "汤", "青菜", "肉菜",
                "少油", "少盐", "不辣", "微辣", "打包", "外卖", "筷子", "勺子",
                "纸巾", "结账", "发票", "洗手间", "服务员", "排队", "座位", "套餐",
                "加饭", "加汤", "已上菜", "请稍等", "空位", "预约", "买单", "现金",
            ),
        ),
        "railway" to SceneTemplate(
            name = "铁路识字",
            description = "火车高铁出行常见字词",
            words = listOf(
                "火车站", "高铁站", "候车室", "检票", "进站", "安检", "行李", "车厢",
                "座号", "卧铺", "硬座", "软座", "站台", "开车", "到达", "晚点",
                "改签", "退票", "车次", "终点", "中转", "身份证", "电子票", "人工窗口",
                "自动检票", "候车", "出站", "换乘", "时间", "发车", "到站", "提醒",
            ),
        ),
        "pharmacy" to SceneTemplate(
            name = "药店识字",
            description = "购药用药常见字词",
            words = listOf(
                "药店", "药师", "感冒药", "止咳药", "退烧药", "胃药", "创可贴", "消毒",
                "棉签", "口罩", "体温计", "说明书", "用法", "用量", "饭前", "饭后",
                "一天三次", "过敏", "慎用", "儿童", "成人", "颗粒", "胶囊", "药片",
                "喷雾", "外用", "内服", "复方", "有效期", "禁忌", "咨询", "处方药",
            ),
        ),
        "wechat" to SceneTemplate(
            name = "微信识字",
            description = "微信沟通和支付常见字词",
            words = listOf(
                "微信", "聊天", "语音", "视频", "通话", "群聊", "朋友圈", "头像",
                "昵称", "消息", "转发", "收藏", "文件", "图片", "表情", "扫一扫",
                "付款", "收款", "红包", "转账", "小程序", "置顶", "删除", "备注",
                "联系人", "添加", "搜索", "扫一扫支付", "朋友圈可见", "隐私", "通知", "提醒",
            ),
        ),
        "weather" to SceneTemplate(
            name = "天气时间识字",
            description = "看天气和时间常见字词",
            words = listOf(
                "今天", "明天", "阴", "晴", "雨", "雪", "多云", "风",
                "温度", "最高", "最低", "早上", "中午", "晚上", "现在", "时间",
                "日期", "星期", "月份", "年份", "闹钟", "提醒", "准时", "延迟",
                "降温", "升温", "出门", "带伞", "加衣", "注意保暖", "空气", "湿度",
            ),
        ),
        "clothing" to SceneTemplate(
            name = "衣物洗护识字",
            description = "穿衣洗护常见字词",
            words = listOf(
                "衣服", "裤子", "鞋子", "袜子", "外套", "毛衣", "衬衫", "帽子",
                "围巾", "手套", "洗衣机", "晾衣架", "洗衣液", "柔顺剂", "漂洗", "脱水",
                "烘干", "折叠", "收纳", "纽扣", "拉链", "大号", "小号", "干净",
                "脏", "换洗", "熨烫", "口袋", "衣柜", "拖鞋", "雨衣", "围裙",
            ),
        ),
        "gov" to SceneTemplate(
            name = "政务办事识字",
            description = "政务大厅常见字词",
            words = listOf(
                "政务大厅", "身份证", "户口本", "社保", "医保", "预约", "排队", "取号",
                "窗口", "表格", "填写", "复印", "盖章", "签字", "办理", "受理",
                "完成", "工作日", "材料", "原件", "复印件", "咨询台", "叫号", "进度",
                "证明", "申请", "审核", "结果", "通知", "领取", "大厅", "服务",
            ),
        ),
        "emergency" to SceneTemplate(
            name = "应急求助识字",
            description = "紧急情况求助常见字词",
            words = listOf(
                "求助", "帮忙", "报警", "119", "120", "110", "火警", "急救",
                "迷路", "走失", "联系", "家人", "电话", "地址", "路口", "医院",
                "药店", "派出所", "危险", "着火", "受伤", "摔倒", "救护车", "担架",
                "别慌", "冷静", "呼救", "定位", "发送位置", "等待", "安全", "应急",
            ),
        ),
        "signs" to SceneTemplate(
            name = "公共标识识字",
            description = "公共场所标识常见字词",
            words = listOf(
                "禁止", "通行", "止步", "小心台阶", "小心地滑", "入口", "出口", "厕所",
                "男", "女", "无障碍", "电梯", "楼梯", "安全出口", "消防栓", "配电房",
                "停车场", "请排队", "请勿吸烟", "保持安静", "施工", "绕行", "危险", "注意",
                "扶梯", "上行", "下行", "服务台", "售票处", "候车室", "检票口", "通道",
            ),
        ),
        "numbers" to SceneTemplate(
            name = "数字金额识字",
            description = "数字和金额常见字词",
            words = listOf(
                "一", "二", "三", "四", "五", "六", "七", "八",
                "九", "十", "百", "千", "万", "元", "角", "分",
                "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒",
                "捌", "玖", "拾", "佰", "仟", "金额", "数量", "合计",
            ),
        ),
        "hotel" to SceneTemplate(
            name = "住宿出行识字",
            description = "酒店住宿常见字词",
            words = listOf(
                "酒店", "前台", "订房", "退房", "房卡", "电梯", "楼层", "早餐",
                "无烟房", "押金", "发票", "身份证", "叫醒", "服务", "热水", "空调",
                "毛巾", "被子", "枕头", "WiFi", "网络", "门牌", "客房", "清洁",
                "续住", "入住", "行李", "寄存", "电话", "导航", "地址", "时间",
            ),
        ),
    )
}
