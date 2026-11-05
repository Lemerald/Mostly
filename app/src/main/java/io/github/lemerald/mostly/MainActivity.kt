package io.github.lemerald.mostly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

// Top-level views per docs/user-stories.md "App views" decision.
private enum class PlannerView(val label: String, val icon: ImageVector) {
    Calendar("Calendar", Icons.Filled.Event),
    Todos("To-dos", Icons.Filled.Checklist),
    Timetable("Timetable", Icons.Filled.ViewWeek),
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                var selected by remember { mutableStateOf(PlannerView.Calendar) }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            PlannerView.entries.forEach { view ->
                                NavigationBarItem(
                                    selected = selected == view,
                                    onClick = { selected = view },
                                    icon = { Icon(view.icon, contentDescription = view.label) },
                                    label = { Text(view.label) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
                        val label = when (selected) {
                            PlannerView.Calendar -> "Calendar view"
                            PlannerView.Todos -> "To-dos view"
                            PlannerView.Timetable -> "Timetable view"
                        }
                        Text(text = label, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}
