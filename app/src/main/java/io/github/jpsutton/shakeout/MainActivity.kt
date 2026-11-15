package io.github.jpsutton.shakeout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.jpsutton.shakeout.ui.GameScreen
import io.github.jpsutton.shakeout.ui.GameViewModel
import io.github.jpsutton.shakeout.ui.MenuScreen
import io.github.jpsutton.shakeout.ui.RulesScreen
import io.github.jpsutton.shakeout.ui.ShakeoutTheme

class MainActivity : ComponentActivity() {
    private val vm: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ShakeoutTheme {
                Surface {
                    var showRules by rememberSaveable { mutableStateOf(false) }
                    when {
                        vm.game != null -> {
                            BackHandler { vm.toMenu() }
                            GameScreen(vm, onMenu = vm::toMenu)
                        }
                        showRules -> {
                            BackHandler { showRules = false }
                            RulesScreen(onBack = { showRules = false })
                        }
                        else -> MenuScreen(vm, onRules = { showRules = true })
                    }
                }
            }
        }
    }
}
