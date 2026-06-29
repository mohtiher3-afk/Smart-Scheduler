package com.example.ui.features.sync

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.services.CloudSyncManager
import com.example.sync.SyncManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncCenterScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val syncState by CloudSyncManager.syncState.collectAsStateWithLifecycle()
    val lastSyncTime by CloudSyncManager.lastSyncTime.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مركز المزامنة السحابية", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SyncStatusCard(syncState, lastSyncTime)
            }

            item {
                SyncActionsCard(
                    onSyncNow = {
                        SyncManager.scheduleSync(context)
                        // Note: In a real app, the worker would update CloudSyncManager.syncState
                        // For the mock, we can manually trigger the simulator for UI feedback
                        scope.launch {
                            // This is just for UI demo since we are in Phase 08
                            // In real prod, CloudSyncManager would be observing WorkManager status
                            CloudSyncManager.performCloudSync(context, emptyList())
                        }
                    }
                )
            }

            item {
                SyncSettingsCard()
            }

            item {
                CloudUsageCard()
            }
        }
    }
}

@Composable
fun SyncStatusCard(state: CloudSyncManager.SyncState, lastSync: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        when (state) {
                            is CloudSyncManager.SyncState.Success -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            is CloudSyncManager.SyncState.Syncing -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            is CloudSyncManager.SyncState.Error -> Color.Red.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (state) {
                        is CloudSyncManager.SyncState.Success -> Icons.Rounded.CloudDone
                        is CloudSyncManager.SyncState.Syncing -> Icons.Rounded.Sync
                        is CloudSyncManager.SyncState.Error -> Icons.Rounded.CloudOff
                        else -> Icons.Rounded.CloudQueue
                    },
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = when (state) {
                        is CloudSyncManager.SyncState.Success -> Color(0xFF4CAF50)
                        is CloudSyncManager.SyncState.Syncing -> MaterialTheme.colorScheme.primary
                        is CloudSyncManager.SyncState.Error -> Color.Red
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (state) {
                    is CloudSyncManager.SyncState.Success -> "تمت المزامنة بنجاح"
                    is CloudSyncManager.SyncState.Syncing -> "جاري المزامنة..."
                    is CloudSyncManager.SyncState.Error -> "خطأ في المزامنة"
                    else -> "جاهز للمزامنة"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "آخر مزامنة: $lastSync",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (state is CloudSyncManager.SyncState.Syncing) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = state.progress,
                    modifier = Modifier.fillMaxWidth().clip(CircleShape),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = state.message,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SyncActionsCard(onSyncNow: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("إجراءات سريعة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onSyncNow,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Sync, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("مزامنة الآن")
            }
            
            OutlinedButton(
                onClick = { /* Backup logic */ },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Backup, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("إنشاء نسخة احتياطية مشفرة")
            }
        }
    }
}

@Composable
fun SyncSettingsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("إعدادات المزامنة", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(12.dp))
            
            SyncSettingItem("مزامنة تلقائية", "مزامنة البيانات في الخلفية", true)
            SyncSettingItem("المزامنة عبر Wi-Fi فقط", "توفير بيانات الهاتف", false)
            SyncSettingItem("تشفير البيانات", "تأمين البيانات قبل الرفع", true)
        }
    }
}

@Composable
fun SyncSettingItem(title: String, subtitle: String, initialValue: Boolean) {
    var checked by remember { mutableStateOf(initialValue) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = { checked = it })
    }
}

@Composable
fun CloudUsageCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("مساحة التخزين السحابي", fontWeight = FontWeight.Bold)
                Text("1.2 GB / 5 GB", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = 0.24f,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                strokeCap = StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                UsageItem("PDF", "450 MB", Color(0xFFE91E63))
                UsageItem("Notes", "120 MB", Color(0xFF2196F3))
                UsageItem("Voice", "630 MB", Color(0xFFFF9800))
            }
        }
    }
}

@Composable
fun UsageItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
