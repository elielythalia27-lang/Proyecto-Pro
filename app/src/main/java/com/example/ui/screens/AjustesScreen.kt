package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SortOption
import com.example.data.model.ThemeMode
import com.example.ui.theme.AppThemeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    themeColor: AppThemeColor,
    onThemeColorChange: (AppThemeColor) -> Unit,
    defaultFilterType: String,
    onDefaultFilterTypeChange: (String) -> Unit,
    sortOption: SortOption,
    onSortOptionChange: (SortOption) -> Unit,
    onClearCache: () -> Unit,
    maxConcurrentDownloads: Int = 3,
    onMaxConcurrentDownloadsChange: (Int) -> Unit = {},
    catalogLayoutMode: String = "GRID_2",
    onCatalogLayoutModeChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hardwareAcceleration by remember { mutableStateOf(true) }
    var screenGestures by remember { mutableStateOf(true) }
    var autoResume by remember { mutableStateOf(true) }
    var wifiOnlyDownloads by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    val screenBg = if (isDarkTheme) Color(0xFF070B18) else Color(0xFFF1F5F9)
    val cardBg = if (isDarkTheme) Color(0xFF0D1424) else Color.White
    val cardBorder = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFA0AEC0)
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val textSecondary = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF334155)
    val itemBg = if (isDarkTheme) Color(0xFF131C30) else Color(0xFFF1F5F9)
    val dividerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFCBD5E1)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = screenBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.2f else 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Ajustes",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            color = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = screenBg
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Ordenación del Catálogo
            SettingsCategoryHeader(title = "ORDENACIÓN POR DEFECTO", icon = Icons.Default.Sort)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Selecciona el orden inicial de los títulos",
                        fontSize = 13.sp,
                        color = textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    SortOption.entries.forEach { option ->
                        val isSelected = sortOption == option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSortOptionChange(option) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { onSortOptionChange(option) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = textSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = option.displayName + if (option == SortOption.NAME_AZ) " (Por defecto)" else "",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) textPrimary else textSecondary
                            )
                        }
                    }
                }
            }

            // Section 2: Modo Visual (Sistema / Oscuro / Claro)
            SettingsCategoryHeader(title = "MODO DE APARIENCIA", icon = Icons.Default.BrightnessAuto)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Selecciona el modo visual preferido para la aplicación",
                        fontSize = 13.sp,
                        color = textSecondary
                    )

                    val modes = listOf(
                        Triple(ThemeMode.SYSTEM, "Del Sistema (Automático)", Icons.Default.BrightnessAuto),
                        Triple(ThemeMode.DARK, "Modo Oscuro", Icons.Default.DarkMode),
                        Triple(ThemeMode.LIGHT, "Modo Claro", Icons.Default.LightMode)
                    )

                    modes.forEach { (mode, label, icon) ->
                        val isSelected = themeMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.18f else 0.12f)
                                    else itemBg
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onThemeModeChange(mode)
                                    onDarkThemeChange(mode == ThemeMode.DARK)
                                }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else textSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) textPrimary else textSecondary
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    onThemeModeChange(mode)
                                    onDarkThemeChange(mode == ThemeMode.DARK)
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = textSecondary
                                )
                            )
                        }
                    }
                }
            }

            // Section 3: Personalización de Colores y Temas de la App
            SettingsCategoryHeader(title = "TEMAS Y PALETA DE COLORES", icon = Icons.Default.Palette)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Elige el color distintivo de Download Free",
                        fontSize = 13.sp,
                        color = textSecondary
                    )

                    AppThemeColor.entries.forEach { palette ->
                        val isSelected = themeColor == palette
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) palette.primary.copy(alpha = if (isDarkTheme) 0.18f else 0.12f) else Color.Transparent)
                                .clickable { onThemeColorChange(palette) }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(palette.primary)
                                        .border(2.dp, if (isSelected) textPrimary else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = palette.displayName,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textPrimary
                                )
                            }
                            if (isSelected) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = palette.primary.copy(alpha = if (isDarkTheme) 0.3f else 0.15f)
                                ) {
                                    Text(
                                        text = "Activo",
                                        color = palette.primary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Gestor de Descargas Avanzado
            SettingsCategoryHeader(title = "GESTOR DE DESCARGAS", icon = Icons.Default.Download)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Wi-Fi Only
                    SettingsSwitchRow(
                        icon = Icons.Default.Wifi,
                        title = "Descargar solo con Wi-Fi",
                        subtitle = "Evita el consumo de datos móviles en redes móviles.",
                        checked = wifiOnlyDownloads,
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onCheckedChange = {
                            wifiOnlyDownloads = it
                            Toast.makeText(context, if (it) "Descargas limitadas a Wi-Fi" else "Descargas permitidas en cualquier red", Toast.LENGTH_SHORT).show()
                        }
                    )

                    HorizontalDivider(color = dividerColor)

                    // Concurrent downloads limit (1 to 5)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Límite de descargas simultáneas",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = textPrimary
                            )
                            Text(
                                text = "Máximo 5 descargas concurrentes. El resto espera en cola.",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (maxConcurrentDownloads > 1) {
                                        onMaxConcurrentDownloadsChange(maxConcurrentDownloads - 1)
                                    }
                                },
                                enabled = maxConcurrentDownloads > 1,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Menos",
                                    tint = if (maxConcurrentDownloads > 1) textPrimary else textSecondary.copy(alpha = 0.4f)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = if (isDarkTheme) 0.18f else 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "$maxConcurrentDownloads",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    if (maxConcurrentDownloads < 5) {
                                        onMaxConcurrentDownloadsChange(maxConcurrentDownloads + 1)
                                    }
                                },
                                enabled = maxConcurrentDownloads < 5,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Más",
                                    tint = if (maxConcurrentDownloads < 5) textPrimary else textSecondary.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }

            // Section 5: Motor de Reproducción Pro
            SettingsCategoryHeader(title = "REPRODUCTOR PRO", icon = Icons.Default.PlayCircleOutline)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsSwitchRow(
                        icon = Icons.Default.Memory,
                        title = "Decodificación por Hardware (HW+)",
                        subtitle = "Acelera la renderización de video y optimiza el consumo de batería.",
                        checked = hardwareAcceleration,
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onCheckedChange = { hardwareAcceleration = it }
                    )
                    SettingsSwitchRow(
                        icon = Icons.Default.TouchApp,
                        title = "Gestos Táctiles Avanzados",
                        subtitle = "Doble toque ±10s, deslizar para brillo y volumen, mantener presionado para 2x.",
                        checked = screenGestures,
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onCheckedChange = { screenGestures = it }
                    )
                    SettingsSwitchRow(
                        icon = Icons.Default.PlayCircleOutline,
                        title = "Reanudar Reproducción Automática",
                        subtitle = "Guarda la posición exacta en milisegundos para continuar donde lo dejaste.",
                        checked = autoResume,
                        isDarkTheme = isDarkTheme,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        onCheckedChange = { autoResume = it }
                    )
                }
            }

            // Section 6: Almacenamiento y Caché
            SettingsCategoryHeader(title = "ALMACENAMIENTO Y CACHÉ", icon = Icons.Default.CleaningServices)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Limpiar memoria caché",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = textPrimary
                            )
                            Text(
                                text = "Elimina imágenes temporales y restablece la caché local.",
                                fontSize = 12.sp,
                                color = textSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showClearCacheDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Vaciar Caché Ahora", color = textPrimary, fontSize = 13.sp)
                    }
                }
            }

            // Section 7: Enlace Oficial
            SettingsCategoryHeader(title = "CANAL OFICIAL", icon = Icons.Default.Info)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Telegram Canal Oficial
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(itemBg)
                            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/downloadfreeelielet"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Telegram: @downloadfreeelielet", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_telegram_logo),
                            contentDescription = "Telegram Oficial",
                            modifier = Modifier.size(36.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Canal de Telegram",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = textPrimary
                            )
                            Text(
                                text = "@downloadfreeelielet",
                                fontSize = 12.sp,
                                color = Color(0xFF2AABEE)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Dialog: Clear Cache Confirmation
        if (showClearCacheDialog) {
            AlertDialog(
                onDismissRequest = { showClearCacheDialog = false },
                containerColor = if (isDarkTheme) Color(0xFF0D1424) else Color.White,
                title = { Text("¿Vaciar memoria caché?", color = textPrimary) },
                text = {
                    Text(
                        "Se eliminarán las imágenes temporales almacenadas. Los videos ya descargados no se verán afectados.",
                        color = textSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearCache()
                            showClearCacheDialog = false
                            Toast.makeText(context, "Caché limpiada con éxito", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Confirmar", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCacheDialog = false }) {
                        Text("Cancelar", color = textSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsCategoryHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 8.dp, start = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    isDarkTheme: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = textSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = if (isDarkTheme) Color(0xFF64748B) else Color(0xFF94A3B8),
                uncheckedTrackColor = if (isDarkTheme) Color(0xFF1E293B) else Color(0xFFE2E8F0)
            )
        )
    }
}
