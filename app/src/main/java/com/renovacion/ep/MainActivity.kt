package com.renovacion.ep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

enum class ModuloApp(val titulo: String, val icono: ImageVector) {
    NOTAS("Notas", Icons.Default.Edit),
    CONSULTA_GLOBAL("Consulta Global", Icons.Default.Search),
    FUENTES("Biblioteca de Fuentes", Icons.Default.List),
    MARCADORES("Marcadores", Icons.Default.Star),
    CONFIGURACION("Ajustes", Icons.Default.Settings)
}

private data class NotaKeepModelo(
    val id: String,
    val titulo: String,
    val contenido: String,
    val colorLight: Color,
    val colorDark: Color
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContenidoPrincipal()
        }
    }
}

@Composable
private fun AppContenidoPrincipal() {
    val esOscuro = isSystemInDarkTheme()
    val colorScheme = if (esOscuro) darkColorScheme() else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppConMenuLateral(esOscuro = esOscuro)
        }
    }
}

@Composable
private fun AppConMenuLateral(esOscuro: Boolean) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var moduloActual by remember { mutableStateOf(ModuloApp.NOTAS) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "EP Erudito Profundo",
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                
                ModuloApp.entries.forEach { modulo ->
                    NavigationDrawerItem(
                        icon = { Icon(modulo.icono, contentDescription = modulo.titulo) },
                        label = { Text(modulo.titulo) },
                        selected = moduloActual == modulo,
                        onClick = {
                            moduloActual = modulo
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        when (moduloActual) {
            ModuloApp.NOTAS -> PantallaNotasPrincipal(
                esOscuro = esOscuro,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
            else -> PantallaModuloGenerico(
                titulo = moduloActual.titulo,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaNotasPrincipal(esOscuro: Boolean, onOpenDrawer: () -> Unit) {
    var textoBusqueda by remember { mutableStateOf("") }
    
    val notasEjemplo = remember {
        listOf(
            NotaKeepModelo("1", "Mateo 24:36", "Pero del día y la hora nadie sabe, ni aun los ángeles de los cielos, sino sólo mi Padre.", Color(0xFFFFF4B8), Color(0xFF4A441D)),
            NotaKeepModelo("2", "Idea de Estudio", "Revisar los términos en griego para 'Parusía' en las notas de consulta.", Color(0xFFE6F4EA), Color(0xFF1E3A29)),
            NotaKeepModelo("3", "Juan 1:1", "En el principio era el Verbo, y el Verbo era con Dios, y el Verbo era Dios.", Color(0xFFE8F0FE), Color(0xFF1D2F4A)),
            NotaKeepModelo("4", "Génesis 1:1", "En el principio creó Dios los cielos y la tierra.", Color(0xFFF3E8FD), Color(0xFF38214A)),
            NotaKeepModelo("5", "Recordatorio", "Agregar referencias faltantes del manuscrito Masorético.", Color(0xFFFCE8E6), Color(0xFF4A2121))
        )
    }

    val notasFiltradas = notasEjemplo.filter {
        it.titulo.contains(textoBusqueda, ignoreCase = true) ||
        it.contenido.contains(textoBusqueda, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Nota")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    TextField(
                        value = textoBusqueda,
                        onValueChange = { textoBusqueda = it },
                        placeholder = { Text("Buscar en tus notas...", fontSize = 15.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Search, 
                        contentDescription = "Buscar", 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(notasFiltradas) { nota ->
                    val colorTarjeta = if (esOscuro) nota.colorDark else nota.colorLight
                    val colorTexto = if (esOscuro) Color.White else Color.Black
                    val colorSubtexto = if (esOscuro) Color(0xFFE1E2E1) else Color(0xFF3C4043)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = colorTarjeta)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = nota.titulo,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorTexto
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = nota.contenido,
                                fontSize = 13.sp,
                                color = colorSubtexto
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaModuloGenerico(titulo: String, onOpenDrawer: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Módulo: $titulo",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
