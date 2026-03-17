package com.peimama.renzi.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.peimama.renzi.data.model.ExerciseType
import com.peimama.renzi.di.AppContainer
import com.peimama.renzi.ui.screen.family.CaregiverScreen
import com.peimama.renzi.ui.screen.home.HomeScreen
import com.peimama.renzi.ui.screen.lesson.LessonFlowScreen
import com.peimama.renzi.ui.screen.lesson.LessonPreviewScreen
import com.peimama.renzi.ui.screen.notebook.WordBookScreen
import com.peimama.renzi.ui.screen.review.ReviewScreen
import com.peimama.renzi.ui.screen.scene.LessonListScreen
import com.peimama.renzi.ui.screen.scene.SceneListScreen
import com.peimama.renzi.ui.viewmodel.FamilyViewModel
import com.peimama.renzi.ui.viewmodel.HomeViewModel
import com.peimama.renzi.ui.viewmodel.LessonFlowViewModel
import com.peimama.renzi.ui.viewmodel.LessonPreviewViewModel
import com.peimama.renzi.ui.viewmodel.NotebookViewModel
import com.peimama.renzi.ui.viewmodel.ReviewViewModel
import com.peimama.renzi.ui.viewmodel.SceneLessonsViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    container: AppContainer,
    innerPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
        modifier = modifier,
    ) {
        composable(AppRoutes.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.factory(container.repository),
            )
            HomeScreen(
                uiState = viewModel.uiState,
                contentPadding = innerPadding,
                onOpenSceneList = { navController.navigate(AppRoutes.SCENE_LIST) },
                onOpenLessonList = { sceneId -> navController.navigate(AppRoutes.lessonList(sceneId)) },
                onOpenLesson = { lessonId -> navController.navigate(AppRoutes.lessonPreview(lessonId)) },
                onOpenReview = { navController.navigate(AppRoutes.REVIEW) },
            )
        }

        composable(AppRoutes.SCENE_LIST) {
            val viewModel: HomeViewModel = viewModel(
                key = "scene_list_vm",
                factory = HomeViewModel.factory(container.repository),
            )
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            SceneListScreen(
                scenes = uiState.scenes,
                contentPadding = innerPadding,
                onBack = { navController.popBackStack() },
                onOpenLessonList = { sceneId ->
                    navController.navigate(AppRoutes.lessonList(sceneId))
                },
            )
        }

        composable(AppRoutes.NOTEBOOK) {
            val viewModel: NotebookViewModel = viewModel(
                factory = NotebookViewModel.factory(container.repository),
            )
            WordBookScreen(
                uiState = viewModel.uiState,
                contentPadding = innerPadding,
                onToggleFavorite = viewModel::toggleFavorite,
                onOpenLesson = { lessonId -> navController.navigate(AppRoutes.lessonPreview(lessonId)) },
            )
        }

        composable(AppRoutes.FAMILY) {
            val viewModel: FamilyViewModel = viewModel(
                factory = FamilyViewModel.factory(container.repository),
            )
            CaregiverScreen(
                uiState = viewModel.uiState,
                contentPadding = innerPadding,
            )
        }

        composable(AppRoutes.REVIEW) {
            val viewModel: ReviewViewModel = viewModel(
                factory = ReviewViewModel.factory(container.repository),
            )
            ReviewScreen(
                uiState = viewModel.uiState,
                onStartReview = { lessonId -> navController.navigate(AppRoutes.lessonFlow(lessonId)) },
                onBackHome = { navController.popBackStack() },
            )
        }

        composable(
            route = AppRoutes.LESSON_LIST,
            arguments = listOf(navArgument("sceneId") { type = NavType.StringType }),
        ) { entry ->
            val sceneId = entry.arguments?.getString("sceneId").orEmpty()
            val viewModel: SceneLessonsViewModel = viewModel(
                key = "lesson_list_$sceneId",
                factory = SceneLessonsViewModel.factory(sceneId, container.repository),
            )
            LessonListScreen(
                uiState = viewModel.uiState,
                onBack = { navController.popBackStack() },
                onPreviewLesson = { lessonId ->
                    navController.navigate(AppRoutes.lessonPreview(lessonId))
                },
                onStartLesson = { lessonId ->
                    viewModel.startLesson(lessonId)
                    navController.navigate(AppRoutes.lessonFlow(lessonId))
                },
            )
        }

        composable(
            route = AppRoutes.LESSON_PREVIEW,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
        ) { entry ->
            val lessonId = entry.arguments?.getString("lessonId").orEmpty()
            val viewModel: LessonPreviewViewModel = viewModel(
                key = "preview_$lessonId",
                factory = LessonPreviewViewModel.factory(
                    lessonId = lessonId,
                    repository = container.repository,
                    audioManager = container.audioManager,
                ),
            )

            LessonPreviewScreen(
                uiState = viewModel.uiState,
                messageFlow = viewModel.messageFlow,
                onBack = { navController.popBackStack() },
                onNextWord = viewModel::nextWord,
                onPreviousWord = viewModel::previousWord,
                onPlayWord = viewModel::playCurrentWord,
                onStartLesson = {
                    viewModel.startLesson()
                    navController.navigate(AppRoutes.lessonFlow(lessonId))
                },
            )
        }

        composable(
            route = AppRoutes.LESSON_FLOW,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
        ) { entry ->
            val lessonId = entry.arguments?.getString("lessonId").orEmpty()
            val viewModel: LessonFlowViewModel = viewModel(
                key = "lesson_$lessonId",
                factory = LessonFlowViewModel.factory(
                    lessonId = lessonId,
                    repository = container.repository,
                    audioManager = container.audioManager,
                    recordingManager = container.recordingManager,
                ),
            )

            LessonFlowScreen(
                uiState = viewModel.uiState,
                messageFlow = viewModel.messageFlow,
                onBack = { navController.popBackStack() },
                onNextStep = viewModel::nextStep,
                onPreviousStep = viewModel::previousStep,
                onNextWord = viewModel::nextWord,
                onPreviousWord = viewModel::previousWord,
                onPlayWord = viewModel::playCurrentWordPronunciation,
                onStartRecording = viewModel::startRecording,
                onStopRecording = viewModel::stopRecording,
                onReplayRecording = viewModel::replayRecording,
                onReRecord = viewModel::reRecord,
                onChooseListenOption = { viewModel.chooseOption(ExerciseType.LISTEN_CHOOSE, it) },
                onChooseImageOption = { viewModel.chooseOption(ExerciseType.IMAGE_CHOOSE, it) },
                onChooseSceneOption = { viewModel.chooseOption(ExerciseType.SCENE_JUDGE, it) },
                onWriteComplete = viewModel::markWriteCompleted,
                onGoReview = {
                    navController.navigate(AppRoutes.REVIEW) {
                        popUpTo(AppRoutes.HOME)
                    }
                },
                onGoHome = {
                    navController.navigate(AppRoutes.HOME) {
                        popUpTo(AppRoutes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

