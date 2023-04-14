package com.example.testapplication

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.cash.molecule.RecompositionClock
import app.cash.molecule.launchMolecule
import com.example.testapplication.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val showFlow = MutableStateFlow(false)

    private val testFlow1 = MutableStateFlow(false)
    private val testFlow2 = MutableStateFlow(false)

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val model: StateFlow<Model> = coroutineScope.launchMolecule(RecompositionClock.Immediate) {
        present()
    }

    private val modelForTextView: StateFlow<ModelForTextView> = coroutineScope.launchMolecule(RecompositionClock.Immediate) {
        presentForTextView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.collect {
                    println("model: $it")
                    triggerAboutViewShowHideLoop(it.func)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                modelForTextView.collect {
                    println("modelForTextView: $it")
                    binding.textView.visibility = if (it.showFlowValue) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                }
            }
        }
    }

    @Volatile var hasStarted = false
    private fun triggerAboutViewShowHideLoop(func: () -> Unit) {
        if (hasStarted) return
        hasStarted = true
        coroutineScope.launch {
            while (true) {
                delay(50)
                println("calling func")
                func()
            }
        }
    }

    @Composable
    fun present(): Model {
        val showFlowValue by showFlow.collectAsState()
        val testFlow1Value by testFlow1.collectAsState()
        val testFlow2Value by testFlow2.collectAsState()

        return Model(
            testFlow1Value = testFlow1Value,
            testFlow2Value = testFlow2Value,
            func = {
                showView(!showFlowValue)
                testFlow1.value = !testFlow1.value
                testFlow2.value = !testFlow2.value
            }
        )
    }

    @Composable
    fun presentForTextView(): ModelForTextView {
        val showFlowValue by showFlow.collectAsState()
        val testFlow1Value by testFlow1.collectAsState()
        val testFlow2Value by testFlow2.collectAsState()

        return ModelForTextView(
            showFlowValue = showFlowValue,
            testFlow1Value = testFlow1Value,
            testFlow2Value = testFlow2Value,
        )
    }

    private fun showView(show: Boolean) {
        showFlow.value = show
    }
}

data class Model(
    val testFlow1Value: Boolean,
    val testFlow2Value: Boolean,
    val func: () -> Unit,
)

data class ModelForTextView(
    val showFlowValue: Boolean,
    val testFlow1Value: Boolean,
    val testFlow2Value: Boolean,
)