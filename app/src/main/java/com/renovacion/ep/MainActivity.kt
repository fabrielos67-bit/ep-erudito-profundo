package com.renovacion.ep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class NotaKeepModelo(
    val id: String,
    val titulo: String,
    val contenido: String,
    val colorPersonalizado: Color? = null
)

data class ConsultaModelo(
    val id: String,
    val termino: String,
    val definicion: String,
    val pasajeReferencia: String
)

data class EnlaceModelo(
    val id: String,
    val titulo: String,
    val url: String,
    val descripcion: String
)

data class MarcadorModelo(
    val id: String,
    val pasaje: String,
    val nota: String
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
    var esVistaCuadricula by remember { mutableStateOf(true) }

    // Notas con color neutro por defecto
    val listaNotas = remember {
        mutableStateListOf(
            NotaKeepModelo("1", "Mateo 24:36", "Pero del día y la hora nadie sabe, ni aun los ángeles de los cielos, sino sólo mi Padre."),
            NotaKeepModelo("2", "Idea de Estudio", "Revisar los términos en griego para 'Parusía' en las notas de consulta."),
            NotaKeepModelo("3", "Juan 1:1", "En el principio era el Verbo, y el Verbo era con Dios, y el Verbo era Dios."),
            NotaKeepModelo("4", "Génesis 1:1", "En el principio creó Dios los cielos y la tierra."),
            NotaKeepModelo("5", "Recordatorio", "Agregar referencias faltantes del manuscrito Masorético.")
        )
    }

    // Datos para Consulta Global
    val listaConsultas = remember {
        mutableStateListOf(
            ConsultaModelo("1", "Parusía (παρουσία)", "Presencia, advenimiento o llegada de una personalidad ilustre o divina.", "Mateo 24:3"),
            ConsultaModelo("2", "Logos (λόγος)", "Palabra, expresión, discurso o razón divina encarnada.", "Juan 1:1"),
            ConsultaModelo("3", "Kenosis (κένωσις)", "El acto de despojarse o vaciarse a sí mismo voluntariamente.", "Filipenses 2:7")
        )
    }

    // Fuentes / Enlaces
    val listaEnlaces = remember {
        mutableStateListOf(
            EnlaceModelo("1", "Codex Sinaiticus Online", "https://codexsinaiticus.org", "Manuscrito bíblico en griego más antiguo."),
            EnlaceModelo("2", "STEP Bible", "https://es.stepbible.org", "Herramienta de análisis interlineal y vocabulario original."),
            EnlaceModelo("3", "Blue Letter Bible", "https://www.blueletterbible.org", "Concordancias Strong, léxicos y comentarios."),
            EnlaceModelo("4", "Perseus Digital Library", "http://www.perseus.tufts.edu", "Textos clásicos y léxicos Liddell-Scott / Short."),
            EnlaceModelo("5", "Septuaginta LXX", "https://www.academic-bible.com", "Texto crítico de la traducción griega del AT."),
            EnlaceModelo("6", "Textus Receptus", "https://tr.org.uk", "Base textual del Nuevo Testamento tradicional."),
            EnlaceModelo("7", "Biblioteca Apostólica Vaticana", "https://www.vaticanlibrary.va", "Manuscritos antiguos y códices digitalizados.")
        )
    }

    // Marcadores
    val listaMarcadores = remember {
        mutableStateListOf(
            MarcadorModelo("1", "Romanos 8:28", "Estudio sobre la Soberanía Divina"),
            MarcadorModelo("2", "Hebreos 11:1", "Definición teológica de la Fe"),
            MarcadorModelo("3", "Isaías 53:5", "Profecía mesiánica del Siervo Sufriente")
        )
    }

    var notaEnEdicion by remember { mutableStateOf<NotaKeepModelo?>(null) }
    var esNuevaNota by remember { mutableStateOf(false) }

    if (notaEnEdicion != null || esNuevaNota) {
        PantallaEdicionNotaCompleta(
            notaInicial = notaEnEdicion,
            onVolver = {
                notaEnEdicion = null
                esNuevaNota = false
            },
            onGuardar = { notaGuardada ->
                if (esNuevaNota) {
                    listaNotas.add(0, notaGuardada)
                } else {
                    val index = listaNotas.indexOfFirst { it.id == notaGuardada.id }
                    if (index != -1) listaNotas[index] = notaGuardada
                }
                notaEnEdicion = null
                esNuevaNota = false
            },
            onEliminar = {
                notaEnEdicion?.let { n -> listaNotas.removeAll { it.id == n.id } }
                notaEnEdicion = null
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
                    listaNotas = listaNotas,
                    esVistaCuadricula = esVistaCuadricula,
                    onToggleVista = { esVistaCuadricula = !esVistaCuadricula },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onCrearNota = { esNuevaNota = true },
                    onEditarNota = { notaEnEdicion = it }
                )
                ModuloApp.CONSULTA_GLOBAL -> PantallaConsultaGlobal(
                    listaConsultas = listaConsultas,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
                ModuloApp.FUENTES -> PantallaEnlacesFuentes(
                    listaEnlaces = listaEnlaces,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
                ModuloApp.MARCADORES -> PantallaMarcadores(
                    listaMarcadores = listaMarcadores,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
                ModuloApp.CONFIGURACION -> PantallaAjustes(
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}

// Pantalla Principal de Notas con Buscador visible y libre de bordes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaNotasPrincipal(
    listaNotas: List<NotaKeepModelo>,
    esVistaCuadricula: Boolean,
    onToggleVista: () -> Unit,
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
                .padding(horizontal = 14.dp)
        ) {
            // Espacio superior para despejar el buscador
            Spacer(modifier = Modifier.height(16.dp))
            
            // Barra de Búsqueda Centrada y Destacada
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 3.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 6.dp)
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
                    
                    IconButton(onClick = onToggleVista) {
                        Icon(
                            imageVector = if (esVistaCuadricula) Icons.Default.List else Icons.Default.Menu,
                            contentDescription = "Cambiar Vista",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val columnas = if (esVistaCuadricula) StaggeredGridCells.Fixed(2) else StaggeredGridCells.Fixed(1)

            LazyVerticalStaggeredGrid(
                columns = columnas,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(notasFiltradas) { nota ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onEditarNota(nota) },
                        colors = CardDefaults.cardColors(
                            containerColor = nota.colorPersonalizado ?: MaterialTheme.colorScheme.surfaceContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            if (nota.titulo.isNotBlank()) {
                                Text(
                                    text = nota.titulo,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            Text(
                                text = nota.contenido,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

// Pantalla Edición/Creación con Tema Adaptable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaEdicionNotaCompleta(
    notaInicial: NotaKeepModelo?,
    onVolver: () -> Unit,
    onGuardar: (NotaKeepModelo) -> Unit,
    onEliminar: () -> Unit
) {
    var titulo by remember { mutableStateOf(notaInicial?.titulo ?: "") }
    var contenido by remember { mutableStateOf(notaInicial?.contenido ?: "") }

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
                                        contenido = contenido
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 22.sp, 
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ),
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
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

// Módulo de Consulta Global
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaConsultaGlobal(
    listaConsultas: MutableList<ConsultaModelo>,
    onOpenDrawer: () -> Unit
) {
    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarDialogoAgregar by remember { mutableStateOf(false) }

    val consultasFiltradas = listaConsultas.filter {
        it.termino.contains(textoBusqueda, ignoreCase = true) ||
        it.definicion.contains(textoBusqueda, ignoreCase = true) ||
        it.pasajeReferencia.contains(textoBusqueda, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consulta Global") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { mostrarDialogoAgregar = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Consulta")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar términos o pasajes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(consultasFiltradas) { consulta ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = consulta.termino,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = consulta.pasajeReferencia,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = consulta.definicion,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoAgregar) {
        DialogoNuevaConsulta(
            onDismiss = { mostrarDialogoAgregar = false },
            onGuardar = { nueva ->
                listaConsultas.add(0, nueva)
                mostrarDialogoAgregar = false
            }
        )
    }
}

@Composable
private fun DialogoNuevaConsulta(
    onDismiss: () -> Unit,
    onGuardar: (ConsultaModelo) -> Unit
) {
    var termino by remember { mutableStateOf("") }
    var pasaje by remember { mutableStateOf("") }
    var definicion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Consulta / Léxico") },
        text = {
            Column {
                OutlinedTextField(
                    value = termino,
                    onValueChange = { termino = it },
                    label = { Text("Término o palabra clave") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = pasaje,
                    onValueChange = { pasaje = it },
                    label = { Text("Pasaje de referencia (Ej. Juan 1:1)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = definicion,
                    onValueChange = { definicion = it },
                    label = { Text("Definición o notas téologicas") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (termino.isNotBlank()) {
                        onGuardar(
                            ConsultaModelo(
                                id = System.currentTimeMillis().toString(),
                                termino = termino,
                                pasajeReferencia = pasaje,
                                definicion = definicion
                            )
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// Módulo de Biblioteca de Fuentes
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaEnlacesFuentes(
    listaEnlaces: MutableList<EnlaceModelo>,
    onOpenDrawer: () -> Unit
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(listaEnlaces) { enlace ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = enlace.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = enlace.descripcion, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = enlace.url, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        DialogoEnlace(
            onDismiss = { mostrarDialogoCrear = false },
            onGuardar = { nuevo ->
                listaEnlaces.add(0, nuevo)
                mostrarDialogoCrear = false
            }
        )
    }
}

@Composable
private fun DialogoEnlace(
    onDismiss: () -> Unit,
    onGuardar: (EnlaceModelo) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Nueva Fuente") },
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
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción corta") },
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
                                id = System.currentTimeMillis().toString(),
                                titulo = titulo,
                                url = url,
                                descripcion = descripcion
                            )
                        )
                    }
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// Módulo de Marcadores
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaMarcadores(
    listaMarcadores: List<MarcadorModelo>,
    onOpenDrawer: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marcadores") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listaMarcadores) { marcador ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 16.dp))
                        Column {
                            Text(text = marcador.pasaje, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = marcador.nota, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

// Módulo de Ajustes Adaptado
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaAjustes(onOpenDrawer: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Apariencia y Tema", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tema del Sistema", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "La aplicación se adapta automáticamente al tema Claro u Oscuro seleccionado en los ajustes de tu dispositivo Android.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Preferencias de Notas", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Color de Tarjetas por Defecto", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Formato neutro activado para integrarse perfectamente con el modo oscuro.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
