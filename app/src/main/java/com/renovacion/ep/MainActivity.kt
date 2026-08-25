package com.renovacion.ep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
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
    FUENTES("Biblioteca de Fuentes (Enlaces)", Icons.Default.List),
    MARCADORES("Marcadores", Icons.Default.Star),
    CONFIGURACION("Ajustes", Icons.Default.Settings)
}

data class NotaKeepModelo(
    val id: String,
    val titulo: String,
    val contenido: String,
    val colorLight: Color,
    val colorDark: Color
)

data class EnlaceModelo(
    val id: String,
    val titulo: String,
    val url: String
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

    // Estado global de notas
    val listaNotas = remember {
        mutableStateListOf(
            NotaKeepModelo("1", "Mateo 24:36", "Pero del día y la hora nadie sabe, ni aun los ángeles de los cielos, sino sólo mi Padre.", Color(0xFFFFF4B8), Color(0xFF4A441D)),
            NotaKeepModelo("2", "Idea de Estudio", "Revisar los términos en griego para 'Parusía' en las notas de consulta.", Color(0xFFE6F4EA), Color(0xFF1E3A29)),
            NotaKeepModelo("3", "Juan 1:1", "En el principio era el Verbo, y el Verbo era con Dios, y el Verbo era Dios.", Color(0xFFE8F0FE), Color(0xFF1D2F4A)),
            NotaKeepModelo("4", "Génesis 1:1", "En el principio creó Dios los cielos y la tierra.", Color(0xFFF3E8FD), Color(0xFF38214A)),
            NotaKeepModelo("5", "Recordatorio", "Agregar referencias faltantes del manuscrito Masorético.", Color(0xFFFCE8E6), Color(0xFF4A2121))
        )
    }

    // Estado global de fuentes / enlaces
    val listaEnlaces = remember {
        mutableStateListOf(
            EnlaceModelo("1", "Codex Sinaiticus", "https://codexsinaiticus.org"),
            EnlaceModelo("2", "Blue Letter Bible", "https://www.blueletterbible.org"),
            EnlaceModelo("3", "STEP Bible", "https://es.stepbible.org")
        )
    }

    // Pantalla completa para edición/creación de nota
    var notaEnEdicionPantallaCompleta by remember { mutableStateOf<NotaKeepModelo?>(null) }
    var esNuevaNota by remember { mutableStateOf(false) }

    if (notaEnEdicionPantallaCompleta != null || esNuevaNota) {
        PantallaEdicionNotaCompleta(
            notaInicial = notaEnEdicionPantallaCompleta,
            esOscuro = esOscuro,
            onVolver = {
                notaEnEdicionPantallaCompleta = null
                esNuevaNota = false
            },
            onGuardar = { notaGuardada ->
                if (esNuevaNota) {
                    listaNotas.add(0, notaGuardada)
                } else {
                    val index = listaNotas.indexOfFirst { it.id == notaGuardada.id }
                    if (index != -1) listaNotas[index] = notaGuardada
                }
                notaEnEdicionPantallaCompleta = null
                esNuevaNota = false
            },
            onEliminar = {
                notaEnEdicionPantallaCompleta?.let { n ->
                    listaNotas.removeAll { it.id == n.id }
                }
                notaEnEdicionPantallaCompleta = null
                esNuevaNota = false
            }
        )
    } else {
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
                    listaNotas = listaNotas,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onCrearNota = { esNuevaNota = true },
                    onEditarNota = { notaEnEdicionPantallaCompleta = it }
                )
                ModuloApp.FUENTES -> PantallaEnlacesFuentes(
                    listaEnlaces = listaEnlaces,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
                else -> PantallaModuloGenerico(
                    titulo = moduloActual.titulo,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaNotasPrincipal(
    esOscuro: Boolean,
    listaNotas: List<NotaKeepModelo>,
    onOpenDrawer: () -> Unit,
    onCrearNota: () -> Unit,
    onEditarNota: (NotaKeepModelo) -> Unit
) {
    var textoBusqueda by remember { mutableStateOf("") }

    val notasFiltradas = listaNotas.filter {
        it.titulo.contains(textoBusqueda, ignoreCase = true) ||
        it.contenido.contains(textoBusqueda, ignoreCase = true)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCrearNota,
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
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onEditarNota(nota) },
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

// Pantalla completa para crear y editar entradas de notas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaEdicionNotaCompleta(
    notaInicial: NotaKeepModelo?,
    esOscuro: Boolean,
    onVolver: () -> Unit,
    onGuardar: (NotaKeepModelo) -> Unit,
    onEliminar: () -> Unit
) {
    var titulo by remember { mutableStateOf(notaInicial?.titulo ?: "") }
    var contenido by remember { mutableStateOf(notaInicial?.contenido ?: "") }
    var colorLightSeleccionado by remember { mutableStateOf(notaInicial?.colorLight ?: Color(0xFFFFF4B8)) }
    var colorDarkSeleccionado by remember { mutableStateOf(notaInicial?.colorDark ?: Color(0xFF4A441D)) }

    val paletaColores = listOf(
        Pair(Color(0xFFFFF4B8), Color(0xFF4A441D)),
        Pair(Color(0xFFE6F4EA), Color(0xFF1E3A29)),
        Pair(Color(0xFFE8F0FE), Color(0xFF1D2F4A)),
        Pair(Color(0xFFF3E8FD), Color(0xFF38214A)),
        Pair(Color(0xFFFCE8E6), Color(0xFF4A2121))
    )

    val fondoPantalla = if (esOscuro) colorDarkSeleccionado else colorLightSeleccionado

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (notaInicial != null) {
                        IconButton(onClick = onEliminar) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Nota", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(
                        onClick = {
                            if (titulo.isNotBlank() || contenido.isNotBlank()) {
                                onGuardar(
                                    NotaKeepModelo(
                                        id = notaInicial?.id ?: System.currentTimeMillis().toString(),
                                        titulo = titulo,
                                        contenido = contenido,
                                        colorLight = colorLightSeleccionado,
                                        colorDark = colorDarkSeleccionado
                                    )
                                )
                            } else {
                                onVolver()
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = fondoPantalla)
            )
        },
        containerColor = fondoPantalla
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            TextField(
                value = titulo,
                onValueChange = { titulo = it },
                placeholder = { Text("Título", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = contenido,
                onValueChange = { contenido = it },
                placeholder = { Text("Escribe una nota...", fontSize = 16.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Color
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Color:", fontWeight = FontWeight.Medium)
                paletaColores.forEach { (cLight, cDark) ->
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cLight)
                            .clickable {
                                colorLightSeleccionado = cLight
                                colorDarkSeleccionado = cDark
                            }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaEnlacesFuentes(
    listaEnlaces: MutableList<EnlaceModelo>,
    onOpenDrawer: () -> Unit
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }
    var enlaceEditar by remember { mutableStateOf<EnlaceModelo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biblioteca de Fuentes") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoCrear = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Enlace")
            }
        }
    ) { padding ->
        if (listaEnlaces.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay enlaces agregados. Toca el botón + para agregar uno.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(listaEnlaces) { enlace ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { enlaceEditar = enlace }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = enlace.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = enlace.url, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        DialogoEnlace(
            enlaceInicial = null,
            onDismiss = { mostrarDialogoCrear = false },
            onGuardar = { nuevo ->
                listaEnlaces.add(0, nuevo)
                mostrarDialogoCrear = false
            },
            onEliminar = null
        )
    }

    enlaceEditar?.let { enlace ->
        DialogoEnlace(
            enlaceInicial = enlace,
            onDismiss = { enlaceEditar = null },
            onGuardar = { editado ->
                val index = listaEnlaces.indexOfFirst { it.id == enlace.id }
                if (index != -1) listaEnlaces[index] = editado
                enlaceEditar = null
            },
            onEliminar = {
                listaEnlaces.removeAll { it.id == enlace.id }
                enlaceEditar = null
            }
        )
    }
}

@Composable
private fun DialogoEnlace(
    enlaceInicial: EnlaceModelo?,
    onDismiss: () -> Unit,
    onGuardar: (EnlaceModelo) -> Unit,
    onEliminar: (() -> Unit)?
) {
    var titulo by remember { mutableStateOf(enlaceInicial?.titulo ?: "") }
    var url by remember { mutableStateOf(enlaceInicial?.url ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (enlaceInicial == null) "Agregar Nuevo Enlace / Fuente" else "Editar Enlace") },
        text = {
            Column {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Nombre de la fuente") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL (https://...)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titulo.isNotBlank() && url.isNotBlank()) {
                        onGuardar(
                            EnlaceModelo(
                                id = enlaceInicial?.id ?: System.currentTimeMillis().toString(),
                                titulo = titulo,
                                url = url
                            )
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            Row {
                if (onEliminar != null) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
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
