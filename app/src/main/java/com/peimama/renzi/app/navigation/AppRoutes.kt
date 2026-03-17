package com.peimama.renzi.app.navigation

object AppRoutes {
    const val HOME = "home"
    const val SCENE_LIST = "scene_list"
    const val NOTEBOOK = "notebook"
    const val FAMILY = "family"
    const val REVIEW = "review"

    const val LESSON_LIST = "lesson_list/{sceneId}"
    const val LESSON_PREVIEW = "lesson_preview/{lessonId}"
    const val LESSON_FLOW = "lesson/{lessonId}"

    fun lessonList(sceneId: String): String = "lesson_list/$sceneId"
    fun lessonPreview(lessonId: String): String = "lesson_preview/$lessonId"
    fun lessonFlow(lessonId: String): String = "lesson/$lessonId"
}
