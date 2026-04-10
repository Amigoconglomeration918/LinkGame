package com.example.linkgame.game.engine

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkgame.audio.AudioManager
import com.example.linkgame.data.model.LeaderboardEntry
import com.example.linkgame.data.repository.LeaderboardRepository
import com.example.linkgame.data.repository.NicknameRepository
import com.example.linkgame.game.logic.*
import com.example.linkgame.game.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameController(
    private val context: Context,
    private val mode: GameMode
) : ViewModel() {

    data class UiState(
        val board: Board,
        val selectedFirst: Pair<Int, Int>? = null,
        val selectedSecond: Pair<Int, Int>? = null,
        val pathCoords: List<Pair<Int, Int>>? = null,
        val hintFirst: Pair<Int, Int>? = null,
        val hintSecond: Pair<Int, Int>? = null,
        val score: Int = 0,
        val timeLeft: Int,
        val levelCleared: Boolean = false,
        val gameFinished: Boolean = false,
        val showSaveDialog: Boolean = false,
        val showNicknameForSave: Boolean = false,
        val showExitGameDialog: Boolean = false,
        val totalTimeSeconds: Int = 0,
        val title: String = "",
        val challengeIndex: Int = 0,
        val endlessLevelNum: Int = 1,
        val currentConfig: LevelConfig,
        val remainingPairs: Int = 0   // 新增：剩余配对数量
    )

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<UiState> = _uiState

    private var startTime = System.currentTimeMillis()
    private var timerJob: kotlinx.coroutines.Job? = null
    private var returnCallback: (() -> Unit)? = null

    // 暂停相关
    private var isPaused = false
    private var pausedDuration = 0L
    private var pauseTimestamp = 0L

    fun setReturnCallback(callback: (() -> Unit)?) {
        returnCallback = callback
    }

    private fun createInitialState(): UiState {
        val config = when (mode) {
            is GameMode.Challenge -> ALL_LEVELS[0]
            is GameMode.Endless -> mode.difficulty
        }
        val board = generateSolvableBoardForLevel(config)
        return UiState(
            board = board,
            timeLeft = config.timeLimit,
            currentConfig = config,
            title = buildTitle(config, endlessLevelNum = 1, challengeIndex = 0),
            remainingPairs = calculateRemainingPairs(board)   // 初始化剩余配对数
        )
    }

    private fun buildTitle(config: LevelConfig, endlessLevelNum: Int = 1, challengeIndex: Int = 0): String {
        return when (mode) {
            is GameMode.Challenge -> "挑战模式 - ${config.name}"
            is GameMode.Endless -> "无尽模式 - ${config.name} - 第 $endlessLevelNum 关"
        }
    }

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val state = _uiState.value
                if (state.gameFinished || state.levelCleared) break
                if (isPaused) continue
                if (state.timeLeft > 0) {
                    _uiState.value = state.copy(timeLeft = state.timeLeft - 1)
                }
                if (_uiState.value.timeLeft == 0 && !state.levelCleared && !state.gameFinished) {
                    finishGame()
                }
            }
        }
    }

    fun pauseGame() {
        if (!isPaused) {
            isPaused = true
            pauseTimestamp = System.currentTimeMillis()
        }
    }

    fun resumeGame() {
        if (isPaused && pauseTimestamp > 0) {
            pausedDuration += (System.currentTimeMillis() - pauseTimestamp)
            pauseTimestamp = 0
        }
        isPaused = false
    }

    fun showHint() {
        val state = _uiState.value
        if (state.levelCleared || state.gameFinished || isPaused) return

        val hintPair = findHintPair(state.board)
        if (hintPair != null) {
            val (p1, p2) = hintPair
            _uiState.value = state.copy(hintFirst = p1, hintSecond = p2)

            viewModelScope.launch {
                delay(2000)
                _uiState.value = _uiState.value.copy(hintFirst = null, hintSecond = null)
            }
        }
    }

    private fun findHintPair(board: Board): Pair<Pair<Int, Int>, Pair<Int, Int>>? {
        for (r1 in 0 until board.rows) {
            for (c1 in 0 until board.cols) {
                val v1 = board.cells[r1][c1]
                if (v1 == 0) continue
                for (r2 in 0 until board.rows) {
                    for (c2 in 0 until board.cols) {
                        if (r1 == r2 && c1 == c2) continue
                        val v2 = board.cells[r2][c2]
                        if (v2 == v1 && canConnectWithPadding(board, r1, c1, r2, c2)) {
                            return Pair(Pair(r1, c1), Pair(r2, c2))
                        }
                    }
                }
            }
        }
        return null
    }

    fun onTileClick(r: Int, c: Int) {
        val state = _uiState.value
        if (state.levelCleared || state.gameFinished || isPaused) return

        if (state.selectedFirst == null) {
            AudioManager.playClick()
            _uiState.value = state.copy(selectedFirst = Pair(r, c))
        } else if (state.selectedFirst?.first == r && state.selectedFirst?.second == c) {
            _uiState.value = state.copy(selectedFirst = null)
        } else {
            val first = state.selectedFirst!!
            val second = Pair(r, c)
            _uiState.value = state.copy(selectedSecond = second)

            if (state.board.cells[first.first][first.second] == state.board.cells[second.first][second.second] &&
                canConnectWithPadding(state.board, first.first, first.second, second.first, second.second)
            ) {
                AudioManager.playEliminate()
                val path = findConnectPath(state.board, first.first, first.second, second.first, second.second)
                _uiState.value = state.copy(pathCoords = path ?: listOf(first, second))

                val newBoard = removeTiles(state.board, first.first, first.second, second.first, second.second)
                val newScore = state.score + 10
                val cleared = isBoardCleared(newBoard)
                val newRemainingPairs = calculateRemainingPairs(newBoard)

                _uiState.value = _uiState.value.copy(
                    board = newBoard,
                    score = newScore,
                    selectedFirst = null,
                    selectedSecond = null,
                    levelCleared = cleared,
                    remainingPairs = newRemainingPairs   // 更新剩余配对数
                )

                if (cleared) {
                    handleLevelCleared()
                }

                viewModelScope.launch {
                    delay(1000)
                    _uiState.value = _uiState.value.copy(pathCoords = null)
                }
            } else {
                _uiState.value = state.copy(
                    selectedFirst = null,
                    selectedSecond = null,
                    pathCoords = null
                )
            }
        }
    }

    private fun handleLevelCleared() {
        val state = _uiState.value
        if (mode is GameMode.Challenge) {
            val nextIndex = state.challengeIndex + 1
            if (nextIndex >= ALL_LEVELS.size) {
                finishGame()
            } else {
                val nextConfig = ALL_LEVELS[nextIndex]
                val newBoard = generateSolvableBoardForLevel(nextConfig)
                _uiState.value = state.copy(
                    challengeIndex = nextIndex,
                    currentConfig = nextConfig,
                    board = newBoard,
                    timeLeft = nextConfig.timeLimit,
                    score = state.score,
                    levelCleared = false,
                    title = buildTitle(nextConfig, challengeIndex = nextIndex),
                    remainingPairs = calculateRemainingPairs(newBoard)   // 新关卡剩余配对数
                )
                startTimer()
            }
        } else if (mode is GameMode.Endless) {
            val newLevelNum = state.endlessLevelNum + 1
            val newBoard = generateSolvableBoardForLevel(state.currentConfig)
            _uiState.value = state.copy(
                endlessLevelNum = newLevelNum,
                board = newBoard,
                timeLeft = state.currentConfig.timeLimit,
                levelCleared = false,
                title = buildTitle(state.currentConfig, endlessLevelNum = newLevelNum),
                remainingPairs = calculateRemainingPairs(newBoard)   // 新关卡剩余配对数
            )
            startTimer()
        }
    }

    fun nextLevel() {
        val state = _uiState.value
        if (state.levelCleared && !state.gameFinished) {
            if (mode is GameMode.Challenge && state.challengeIndex == ALL_LEVELS.size - 1) {
                finishGame()
            } else {
                handleLevelCleared()
            }
        }
    }

    fun showExitGameDialog() {
        pauseGame()
        _uiState.value = _uiState.value.copy(showExitGameDialog = true)
    }

    fun dismissExitGameDialog() {
        _uiState.value = _uiState.value.copy(showExitGameDialog = false)
        if (!_uiState.value.gameFinished) {
            resumeGame()
        }
    }

    private fun endGame() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(gameFinished = true)
    }

    private fun finishGame() {
        endGame()
        _uiState.value = _uiState.value.copy(showExitGameDialog = true)
    }

    fun exitGame() {
        showExitGameDialog()
    }

    fun returnWithoutSaving() {
        dismissExitGameDialog()
        endGame()
        returnCallback?.invoke()
    }

    fun saveScoreAndReturn() {
        viewModelScope.launch {
            val state = _uiState.value
            val nickname = NicknameRepository.getNickname(context)
            if (nickname.isNullOrBlank()) {
                _uiState.value = state.copy(
                    showExitGameDialog = false,
                    showNicknameForSave = true
                )
            } else {
                saveScoreWithNickname(nickname)
            }
        }
    }

    fun saveScoreWithNickname(nickname: String) {
        viewModelScope.launch {
            val state = _uiState.value
            NicknameRepository.saveNickname(context, nickname)
            val totalSeconds = ((System.currentTimeMillis() - startTime - pausedDuration) / 1000).toInt()
            val entry = LeaderboardEntry(
                nickname = nickname,
                score = state.score,
                timeSeconds = totalSeconds,
                mode = when (mode) {
                    is GameMode.Challenge -> "challenge"
                    is GameMode.Endless -> "endless"
                },
                difficulty = if (mode is GameMode.Endless) state.currentConfig.name else null
            )
            LeaderboardRepository.addEntry(context, entry)

            _uiState.value = state.copy(
                showExitGameDialog = false,
                showNicknameForSave = false,
                gameFinished = true
            )
            endGame()
            returnCallback?.invoke()
        }
    }

    fun dismissSaveDialogAndReturn() {
        _uiState.value = _uiState.value.copy(
            showSaveDialog = false,
            gameFinished = true
        )
        returnCallback?.invoke()
    }

    fun dismissSaveDialog() {
        _uiState.value = _uiState.value.copy(showSaveDialog = false)
    }

    fun dismissNicknameDialogOnly() {
        _uiState.value = _uiState.value.copy(
            showNicknameForSave = false,
            gameFinished = true
        )
    }

    fun dismissNicknameDialog() {
        _uiState.value = _uiState.value.copy(
            showNicknameForSave = false,
            gameFinished = true
        )
        returnCallback?.invoke()
    }

    // 计算剩余配对数量：统计每个值的出现次数，每个值出现次数除以2并求和
    private fun calculateRemainingPairs(board: Board): Int {
        val countMap = mutableMapOf<Int, Int>()
        for (row in board.cells) {
            for (value in row) {
                if (value != 0) {
                    countMap[value] = countMap.getOrDefault(value, 0) + 1
                }
            }
        }
        return countMap.values.sumOf { it / 2 }
    }
}