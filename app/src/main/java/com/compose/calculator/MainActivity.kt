package com.compose.calculator

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.compose.calculator.ui.theme.CalculatorAppTheme
import java.math.BigDecimal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Calculator()
                }
            }
        }
    }
}


data class CalculatorState(
    val number1: String = "0",
    val number2: String = "0",
    val opt: String? = null,
    val isPreviousOpt: Boolean = false
)

@Composable
fun Calculator() {
    var state by remember {
        mutableStateOf(CalculatorState())
    }

    val buttons = arrayOf(
        arrayOf(
            "AC" to MaterialTheme.colorScheme.tertiary,
            "+/-" to MaterialTheme.colorScheme.tertiary,
            "%" to MaterialTheme.colorScheme.tertiary,
            "/" to MaterialTheme.colorScheme.primary
        ),
        arrayOf(
            "7" to MaterialTheme.colorScheme.secondary,
            "8" to MaterialTheme.colorScheme.secondary,
            "9" to MaterialTheme.colorScheme.secondary,
            "*" to MaterialTheme.colorScheme.primary
        ),
        arrayOf(
            "4" to MaterialTheme.colorScheme.secondary,
            "5" to MaterialTheme.colorScheme.secondary,
            "6" to MaterialTheme.colorScheme.secondary,
            "-" to MaterialTheme.colorScheme.primary
        ),
        arrayOf(
            "1" to MaterialTheme.colorScheme.secondary,
            "2" to MaterialTheme.colorScheme.secondary,
            "3" to MaterialTheme.colorScheme.secondary,
            "+" to MaterialTheme.colorScheme.primary
        ),
        arrayOf(
            "0" to MaterialTheme.colorScheme.secondary,
            "." to MaterialTheme.colorScheme.secondary,
            "=" to MaterialTheme.colorScheme.primary
        )
    )
    Surface {
        Column {
            Box(
                Modifier
                    .fillMaxHeight(0.3f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = if (state.isPreviousOpt) state.number1 else state.number2,
                    fontSize = 80.sp,
                    maxLines = 2,
                    lineHeight = 80.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp)
            ) {
                buttons.forEach {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        it.forEach {
                            CalculatorButton(
                                Modifier
                                    .weight(if (it.first == "0") 2f else 1f)
                                    .aspectRatio(if (it.first == "0") 2f else 1f)
                                    .background(it.second), it.first
                            ) {
                                state = calculate(state, it.first)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(modifier: Modifier, symbol: String, onClick: () -> Unit = {}) {
    Box(
        Modifier
            .clip(RoundedCornerShape(32.dp))
            .then(modifier)
            .clickable { onClick.invoke() }, contentAlignment = Alignment.Center
    ) {
        Text(text = symbol, fontSize = 40.sp, color = MaterialTheme.colorScheme.onPrimary)
    }
}

fun calculate(curState: CalculatorState, input: String): CalculatorState {
    return when (input) {
        in "0".."9" -> curState.copy(
            number2 = if (curState.number2 == "0") input else curState.number2 + input,
            number1 = if (curState.opt == "=") "0" else curState.number1,
            isPreviousOpt = false
        )

        in arrayOf("+", "-", "*", "/") -> if (curState.isPreviousOpt) curState.copy(
            opt = input,
            isPreviousOpt = true
        ) else when (curState.opt) {
            "+" -> curState.copy(
                number1 = BigDecimal(curState.number1).add(BigDecimal(curState.number2)).stripTrailingZeros()
                    .toPlainString(),
                number2 = "0",
                opt = input,
                isPreviousOpt = true
            )
            "-" -> curState.copy(
                number1 = BigDecimal(curState.number1).subtract(BigDecimal(curState.number2)).stripTrailingZeros()
                    .toPlainString(),
                number2 = "0",
                opt = input,
                isPreviousOpt = true
            )
            "*" -> curState.copy(
                number1 = BigDecimal(curState.number1).multiply(BigDecimal(curState.number2)).stripTrailingZeros()
                    .toPlainString(),
                number2 = "0",
                opt = input,
                isPreviousOpt = true
            )
            "/" -> curState.copy(
                number1 = try {
                    BigDecimal(curState.number1).divide(BigDecimal(curState.number2), 16, BigDecimal.ROUND_DOWN)
                        .stripTrailingZeros()
                        .toPlainString()
                } catch (e: Exception) {
                    "0"
                },
                number2 = "0",
                opt = input,
                isPreviousOpt = true
            )
            else -> curState.copy(
                number1 = curState.number2,
                number2 = "0",
                opt = input,
                isPreviousOpt = true
            )
        }
        
        "." -> curState.copy(
            number2 = if (curState.number2.contains(".")) curState.number2 else curState.number2 + input,
            number1 = if (curState.opt == "=") "0" else curState.number1,
            isPreviousOpt = false
        )

        "AC" -> curState.copy(number1 = "0", number2 = "0", opt = null, isPreviousOpt = false)
        
        "%" -> if (curState.isPreviousOpt) curState.copy(
            number1 = BigDecimal(curState.number1).divide(BigDecimal("100")).stripTrailingZeros().toPlainString(),
        ) else curState.copy(
            number2 = BigDecimal(curState.number2).divide(BigDecimal("100")).stripTrailingZeros().toPlainString(),
        )

        "+/-" -> if (curState.isPreviousOpt) curState.copy(
            number1 = BigDecimal(curState.number1).multiply(BigDecimal("-1")).stripTrailingZeros().toPlainString(),
        ) else curState.copy(
            number2 = BigDecimal(curState.number2).multiply(BigDecimal("-1")).stripTrailingZeros().toPlainString(),
        )

        "=" -> when (curState.opt) {
            "+" -> curState.copy(
                number1 = BigDecimal(curState.number1).add(BigDecimal(curState.number2)).stripTrailingZeros()
                    .toPlainString(),
                number2 = "0",
                opt = input,
                isPreviousOpt = true
            )
            "-" -> curState.copy(
                number1 = BigDecimal(curState.number1).subtract(BigDecimal(curState.number2)).stripTrailingZeros()
                    .toPlainString(),
                number2 = "0",
                opt = input,
                isPreviousOpt = true
            )
            "*" -> curState.copy(
                number1 = BigDecimal(curState.number1).multiply(BigDecimal(curState.number2)).stripTrailingZeros()
                    .toPlainString(),
                number2 = "0",
                opt = input,
                isPreviousOpt = true
            )
            "/" -> curState.copy(
                number1 = try {
                    BigDecimal(curState.number1).divide(BigDecimal(curState.number2), 16, BigDecimal.ROUND_DOWN)
                        .stripTrailingZeros()
                        .toPlainString()
                } catch (e: Exception) {
                    "0"
                },
                number2 = "0",
                opt = input,
                isPreviousOpt = true
            )
            else -> curState
        }

        else -> curState
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DefaultPreview() {
    CalculatorAppTheme {
        Calculator()
    }
}

