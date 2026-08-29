package com.renovacion.ep

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class ModuloApp(val titulo: String, val icono: ImageVector) {
    NOTAS("Notas", Icons.Default.Edit),
    CONSULTA_GLOBAL("Consulta Global", Icons.Default.Search),
    FUENTES("Biblioteca de Fuentes", Icons.Default.List),
    MARCADORES("Marcadores", Icons.Default.Star),
    CONFIGURACION("Ajustes", Icons.Default.Settings)
}

enum class ModoTema {
    SISTEMA, CLARO, OSCURO
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
    val pasajeReferencia: String,
    val definicion: String
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
    var mostrarSplash by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        delay(2200)
        mostrarSplash = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (mostrarSplash) {
            PantallaSplashMinimalista()
        } else {
            ContenidoAplicacionReal()
        }
    }
}

@Composable
private fun PantallaSplashMinimalista() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val primaryGreen = Color(0xFF4CAF50)
            val shellGreen = Color(0xFF2E7D32)
            val goldColor = Color(0xFFFFD54F)
            val deskColor = Color(0xFF8D6E63)
            val bookColor = Color(0xFF1E88E5)

            drawArc(
                color = shellGreen,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                size = androidx.compose.ui.geometry.Size(60f, 50f),
                topLeft = Offset(20f, 25f)
            )

            drawCircle(color = primaryGreen, radius = 12f, center = Offset(75f, 38f))
            drawCircle(color = goldColor, radius = 4f, center = Offset(76f, 36f), style = Stroke(width = 2f))
            drawLine(color = deskColor, start = Offset(10f, 65f), end = Offset(90f, 65f), strokeWidth = 4f)
            drawRect(color = Color.White, topLeft = Offset(35f, 52f), size = androidx.compose.ui.geometry.Size(30f, 12f))
            drawLine(color = bookColor, start = Offset(50f, 52f), end = Offset(50f, 64f), strokeWidth = 2f)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "EPPR",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 4.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "DESCIFRANDO LAS ESCRITURAS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Light,
            color = Color(0xFFB0B0B0),
            letterSpacing = 2.sp
        )
    }
}

@Composable
private fun ContenidoAplicacionReal() {
    var modoTemaSeleccionado by remember { mutableStateOf(ModoTema.SISTEMA) }
    val sistemaEsOscuro = isSystemInDarkTheme()

    val esOscuro = when (modoTemaSeleccionado) {
        ModoTema.SISTEMA -> sistemaEsOscuro
        ModoTema.CLARO -> false
        ModoTema.OSCURO -> true
    }

    val darkProfundoColorScheme = darkColorScheme(
        background = Color(0xFF000000),
        surface = Color(0xFF121212),
        surfaceContainer = Color(0xFF1A1A1A),
        surfaceContainerHigh = Color(0xFF242424),
        onBackground = Color(0xFFE0E0E0),
        onSurface = Color(0xFFE0E0E0),
        onSurfaceVariant = Color(0xFFB0B0B0)
    )

    val colorScheme = if (esOscuro) darkProfundoColorScheme else lightColorScheme()

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppConMenuLateral(
                modoTema = modoTemaSeleccionado,
                onCambiarTema = { modoTemaSeleccionado = it }
            )
        }
    }
}

@Composable
private fun AppConMenuLateral(
    modoTema: ModoTema,
    onCambiarTema: (ModoTema) -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("eppr_prefs", Context.MODE_PRIVATE) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var moduloActual by remember { mutableStateOf(ModuloApp.NOTAS) }

    var esVistaCuadricula by remember {
        mutableStateOf(prefs.getBoolean("pref_vista_cuadricula", true))
    }
    var mostrarVistaPreviaNota by remember {
        mutableStateOf(prefs.getBoolean("pref_vista_previa", true))
    }

    val cambiarVistaCuadricula: (Boolean) -> Unit = { nuevaVista ->
        esVistaCuadricula = nuevaVista
        prefs.edit().putBoolean("pref_vista_cuadricula", nuevaVista).apply()
    }

    val cambiarVistaPrevia: (Boolean) -> Unit = { nuevaPrevia ->
        mostrarVistaPreviaNota = nuevaPrevia
        prefs.edit().putBoolean("pref_vista_previa", nuevaPrevia).apply()
    }

    // --- NOTAS: ahora se cargan desde el teléfono (SharedPreferences) ---
    val listaNotas = remember {
        val notasGuardadas = mutableStateListOf<NotaKeepModelo>()
        val json = prefs.getString("notas_json", null)
        if (json != null) {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                notasGuardadas.add(
                    NotaKeepModelo(
                        id = obj.getString("id"),
                        titulo = obj.getString("titulo"),
                        contenido = obj.getString("contenido")
                    )
                )
            }
        } else {
            notasGuardadas.addAll(
                listOf(
                    NotaKeepModelo("1", "Mateo 24:36", "Pero del día y la hora nadie sabe, ni aun los ángeles de los cielos, sino sólo mi Padre."),
                    NotaKeepModelo("2", "Idea de Estudio", "Revisar los términos en griego para 'Parusía' en las notas de consulta."),
                    NotaKeepModelo("3", "Juan 1:1", "En el principio era el Verbo, y el Verbo era con Dios, y el Verbo era Dios."),
                    NotaKeepModelo("4", "Génesis 1:1", "En el principio creó Dios los cielos y la tierra."),
                    NotaKeepModelo("5", "Recordatorio", "Agregar referencias faltantes del manuscrito Masorético.")
                )
            )
        }
        notasGuardadas
    }

    fun guardarNotas() {
        val array = JSONArray()
        listaNotas.forEach { n ->
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("titulo", n.titulo)
            obj.put("contenido", n.contenido)
            array.put(obj)
        }
        prefs.edit().putString("notas_json", array.toString()).apply()
    }
    // --- FIN cambios de Notas ---

    // --- CONSULTA GLOBAL: ahora se carga desde el teléfono (SharedPreferences) ---
    val listaConsultas = remember {
        val consultasGuardadas = mutableStateListOf<ConsultaModelo>()
        val json = prefs.getString("consultas_json", null)
        if (json != null) {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                consultasGuardadas.add(
                    ConsultaModelo(
                        id = obj.getString("id"),
                        termino = obj.getString("termino"),
                        pasajeReferencia = obj.getString("pasajeReferencia"),
                        definicion = obj.getString("definicion")
                    )
                )
            }
        } else {
            consultasGuardadas.addAll(
                listOf(
                    ConsultaModelo("1", "Parusía (παρουσία)", "Mateo 24:3", "Presencia, advenimiento o llegada de una personalidad ilustre o divina."),
                    ConsultaModelo("2", "Logos (λόγος)", "Juan 1:1", "Palabra, expresión, discurso o razón divina encarnada."),
                    ConsultaModelo("3", "Kenosis (κένωσις)", "Filipenses 2:7", "El acto de despojarse o vaciarse a sí mismo voluntariamente.")
                )
            )
        }
        consultasGuardadas
    }

    fun guardarConsultas() {
        val array = JSONArray()
        listaConsultas.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("termino", c.termino)
            obj.put("pasajeReferencia", c.pasajeReferencia)
            obj.put("definicion", c.definicion)
            array.put(obj)
        }
        prefs.edit().putString("consultas_json", array.toString()).apply()
    }
    // --- FIN cambios de Consulta Global ---

    // --- FUENTES: se cargan desde el teléfono (SharedPreferences) ---
    val listaEnlaces = remember {
        val enlacesGuardados = mutableStateListOf<EnlaceModelo>()
        val json = prefs.getString("enlaces_json", null)
        if (json != null) {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                enlacesGuardados.add(
                    EnlaceModelo(
                        id = obj.getString("id"),
                        titulo = obj.getString("titulo"),
                        url = obj.getString("url"),
                        descripcion = obj.getString("descripcion")
                    )
                )
            }
        } else {
            enlacesGuardados.addAll(
                listOf(
                    EnlaceModelo("1", "Codex Sinaiticus Online", "https://codexsinaiticus.org", "Manuscrito bíblico en griego más antiguo."),
                    EnlaceModelo("2", "STEP Bible", "https://es.stepbible.org", "Herramienta de análisis interlineal y vocabulario original."),
                    EnlaceModelo("3", "Blue Letter Bible", "https://www.blueletterbible.org", "Concordancias Strong, léxicos y comentarios."),
                    EnlaceModelo("4", "Perseus Digital Library", "http://www.perseus.tufts.edu", "Textos clásicos y léxicos Liddell-Scott / Short."),
                    EnlaceModelo("5", "Septuaginta LXX", "https://www.academic-bible.com", "Texto crítico de la traducción griega del AT."),
                    EnlaceModelo("6", "Textus Receptus", "https://tr.org.uk", "Base textual del Nuevo Testamento tradicional."),
                    EnlaceModelo("7", "Biblioteca Apostólica Vaticana", "https://www.vaticanlibrary.va", "Manuscritos antiguos y códices digitalizados.")
                )
            )
        }
        enlacesGuardados
    }

    fun guardarEnlaces() {
        val array = JSONArray()
        listaEnlaces.forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("titulo", e.titulo)
            obj.put("url", e.url)
            obj.put("descripcion", e.descripcion)
            array.put(obj)
        }
        prefs.edit().putString("enlaces_json", array.toString()).apply()
    }
    // --- FIN cambios de Fuentes ---

    // --- MARCADORES: se cargan desde el teléfono (SharedPreferences) ---
    val listaMarcadores = remember {
        val marcadoresGuardados = mutableStateListOf<MarcadorModelo>()
        val json = prefs.getString("marcadores_json", null)
        if (json != null) {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                marcadoresGuardados.add(
                    MarcadorModelo(
                        id = obj.getString("id"),
                        pasaje = obj.getString("pasaje"),
                        nota = obj.getString("nota")
                    )
                )
            }
        } else {
            marcadoresGuardados.addAll(
                listOf(
                    MarcadorModelo("1", "Romanos 8:28", "Estudio sobre la Soberanía Divina"),
                    MarcadorModelo("2", "Hebreos 11:1", "Definición teológica de la Fe"),
                    MarcadorModelo("3", "Isaías 53:5", "Profecía mesiánica del Siervo Sufriente")
                )
            )
        }
        marcadoresGuardados
    }

    fun guardarMarcadores() {
        val array = JSONArray()
        listaMarcadores.forEach { m ->
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("pasaje", m.pasaje)
            obj.put("nota", m.nota)
            array.put(obj)
        }
        prefs.edit().putString("marcadores_json", array.toString()).apply()
    }
    // --- FIN cambios de Marcadores ---

    var notaEnEdicion by remember { mutableStateOf<NotaKeepModelo?>(null) }
    var esNuevaNota by remember { mutableStateOf(false) }

    var consultaEnEdicion by remember { mutableStateOf<ConsultaModelo?>(null) }
    var esNuevaConsulta by remember { mutableStateOf(false) }

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
                guardarNotas()
                notaEnEdicion = null
                esNuevaNota = false
            },
            onEliminar = {
                notaEnEdicion?.let { n -> listaNotas.removeAll { it.id == n.id } }
                guardarNotas()
                notaEnEdicion = null
                esNuevaNota = false
            }
        )
    } else if (consultaEnEdicion != null || esNuevaConsulta) {
        PantallaEdicionConsultaCompleta(
            consultaInicial = consultaEnEdicion,
            onVolver = {
                consultaEnEdicion = null
                esNuevaConsulta = false
            },
            onGuardar = { consultaGuardada ->
                if (esNuevaConsulta) {
                    listaConsultas.add(0, consultaGuardada)
                } else {
                    val index = listaConsultas.indexOfFirst { it.id == consultaGuardada.id }
                    if (index != -1) listaConsultas[index] = consultaGuardada
                }
                guardarConsultas()
                consultaEnEdicion = null
                esNuevaConsulta = false
            },
            onEliminar = {
                consultaEnEdicion?.let { c -> listaConsultas.removeAll { it.id == c.id } }
                guardarConsultas()
                consultaEnEdicion = null
                esNuevaConsulta = false
            }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "EPPR",
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
                    mostrarVistaPrevia = mostrarVistaPreviaNota,
                    onToggleVista = { cambiarVistaCuadricula(!esVistaCuadricula) },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onCrearNota = { esNuevaNota = true },
                    onEditarNota = { notaEnEdicion = it }
                )
                ModuloApp.CONSULTA_GLOBAL -> PantallaConsultaGlobal(
                    listaConsultas = listaConsultas,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onCrearConsulta = { esNuevaConsulta = true },
                    onEditarConsulta = { consultaEnEdicion = it }
                )
                ModuloApp.FUENTES -> PantallaEnlacesFuentes(
                    listaEnlaces = listaEnlaces,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onCambio = { guardarEnlaces() }
                )
                ModuloApp.MARCADORES -> PantallaMarcadores(
                    listaMarcadores = listaMarcadores,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onCambio = { guardarMarcadores() }
                )
                ModuloApp.CONFIGURACION -> PantallaAjustesCompleta(
                    modoTema = modoTema,
                    onCambiarTema = onCambiarTema,
                    esVistaCuadricula = esVistaCuadricula,
                    onToggleVistaDefault = cambiarVistaCuadricula,
                    mostrarVistaPrevia = mostrarVistaPreviaNota,
                    onToggleVistaPrevia = cambiarVistaPrevia,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaNotasPrincipal(
    listaNotas: List<NotaKeepModelo>,
    esVistaCuadricula: Boolean,
    mostrarVistaPrevia: Boolean,
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
            Spacer(modifier = Modifier.height(16.dp))

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
                                if (mostrarVistaPrevia) Spacer(modifier = Modifier.height(6.dp))
                            }
                            if (mostrarVistaPrevia) {
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
}

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
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaConsultaGlobal(
    listaConsultas: List<ConsultaModelo>,
    onOpenDrawer: () -> Unit,
    onCrearConsulta: () -> Unit,
    onEditarConsulta: (ConsultaModelo) -> Unit
) {
    var textoBusqueda by remember { mutableStateOf("") }

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
                onClick = onCrearConsulta,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva Consulta")
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onEditarConsulta(consulta) }
                    ) {
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
                                if (consulta.pasajeReferencia.isNotBlank()) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaEdicionConsultaCompleta(
    consultaInicial: ConsultaModelo?,
    onVolver: () -> Unit,
    onGuardar: (ConsultaModelo) -> Unit,
    onEliminar: () -> Unit
) {
    var termino by remember { mutableStateOf(consultaInicial?.termino ?: "") }
    var pasajeReferencia by remember { mutableStateOf(consultaInicial?.pasajeReferencia ?: "") }
    var definicion by remember { mutableStateOf(consultaInicial?.definicion ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (consultaInicial == null) "Nueva Consulta" else "Editar Consulta") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (consultaInicial != null) {
                        IconButton(onClick = onEliminar) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Consulta", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(
                        onClick = {
                            if (termino.isNotBlank() || definicion.isNotBlank()) {
                                onGuardar(
                                    ConsultaModelo(
                                        id = consultaInicial?.id ?: System.currentTimeMillis().toString(),
                                        termino = termino,
                                        pasajeReferencia = pasajeReferencia,
                                        definicion = definicion
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
            OutlinedTextField(
                value = termino,
                onValueChange = { termino = it },
                label = { Text("Término o Palabra Clave (Ej. Logos)") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = pasajeReferencia,
                onValueChange = { pasajeReferencia = it },
                label = { Text("Pasaje Bíblico de Referencia (Ej. Juan 1:1)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = definicion,
                onValueChange = { definicion = it },
                label = { Text("Definición, Notas Lingüísticas o Análisis Teológico") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaEnlacesFuentes(
    listaEnlaces: MutableList<EnlaceModelo>,
    onOpenDrawer: () -> Unit,
    onCambio: () -> Unit
) {
    var enlaceEnEdicion by remember { mutableStateOf<EnlaceModelo?>(null) }
    var esNuevoEnlace by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    if (enlaceEnEdicion != null || esNuevoEnlace) {
        PantallaEdicionEnlaceCompleta(
            enlaceInicial = enlaceEnEdicion,
            onVolver = {
                enlaceEnEdicion = null
                esNuevoEnlace = false
            },
            onGuardar = { enlaceGuardado ->
                if (esNuevoEnlace) {
                    listaEnlaces.add(0, enlaceGuardado)
                } else {
                    val index = listaEnlaces.indexOfFirst { it.id == enlaceGuardado.id }
                    if (index != -1) listaEnlaces[index] = enlaceGuardado
                }
                onCambio()
                enlaceEnEdicion = null
                esNuevoEnlace = false
            },
            onEliminar = {
                enlaceEnEdicion?.let { e -> listaEnlaces.removeAll { it.id == e.id } }
                onCambio()
                enlaceEnEdicion = null
                esNuevoEnlace = false
            }
        )
    } else {
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
                    onClick = { esNuevoEnlace = true },
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                try {
                                    uriHandler.openUri(enlace.url)
                                } catch (e: Exception) {
                                    // Evita cierres si la URL no es válida
                                }
                            }
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
                                Text(text = enlace.descripcion, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = enlace.url, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { enlaceEnEdicion = enlace }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar Fuente",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaEdicionEnlaceCompleta(
    enlaceInicial: EnlaceModelo?,
    onVolver: () -> Unit,
    onGuardar: (EnlaceModelo) -> Unit,
    onEliminar: () -> Unit
) {
    var titulo by remember { mutableStateOf(enlaceInicial?.titulo ?: "") }
    var url by remember { mutableStateOf(enlaceInicial?.url ?: "") }
    var descripcion by remember { mutableStateOf(enlaceInicial?.descripcion ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (enlaceInicial == null) "Nueva Fuente" else "Editar Fuente") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (enlaceInicial != null) {
                        IconButton(onClick = onEliminar) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Fuente", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(
                        onClick = {
                            if (titulo.isNotBlank() && url.isNotBlank()) {
                                onGuardar(
                                    EnlaceModelo(
                                        id = enlaceInicial?.id ?: System.currentTimeMillis().toString(),
                                        titulo = titulo,
                                        url = url,
                                        descripcion = descripcion
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
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Nombre de la fuente") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL (https://...)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción corta") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaMarcadores(
    listaMarcadores: MutableList<MarcadorModelo>,
    onOpenDrawer: () -> Unit,
    onCambio: () -> Unit
) {
    var marcadorEnEdicion by remember { mutableStateOf<MarcadorModelo?>(null) }
    var esNuevoMarcador by remember { mutableStateOf(false) }

    if (marcadorEnEdicion != null || esNuevoMarcador) {
        PantallaEdicionMarcador(
            marcadorInicial = marcadorEnEdicion,
            onVolver = {
                marcadorEnEdicion = null
                esNuevoMarcador = false
            },
            onGuardar = { marcadorGuardado ->
                if (esNuevoMarcador) {
                    listaMarcadores.add(0, marcadorGuardado)
                } else {
                    val index = listaMarcadores.indexOfFirst { it.id == marcadorGuardado.id }
                    if (index != -1) listaMarcadores[index] = marcadorGuardado
                }
                onCambio()
                marcadorEnEdicion = null
                esNuevoMarcador = false
            },
            onEliminar = {
                marcadorEnEdicion?.let { m -> listaMarcadores.removeAll { it.id == m.id } }
                onCambio()
                marcadorEnEdicion = null
                esNuevoMarcador = false
            }
        )
    } else {
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
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { esNuevoMarcador = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Marcador")
                }
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { marcadorEnEdicion = marcador }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = marcador.pasaje, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (marcador.nota.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = marcador.nota,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
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
private fun PantallaEdicionMarcador(
    marcadorInicial: MarcadorModelo?,
    onVolver: () -> Unit,
    onGuardar: (MarcadorModelo) -> Unit,
    onEliminar: () -> Unit
) {
    var pasaje by remember { mutableStateOf(marcadorInicial?.pasaje ?: "") }
    var nota by remember { mutableStateOf(marcadorInicial?.nota ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (marcadorInicial == null) "Nuevo Marcador" else "Editar Marcador") },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (marcadorInicial != null) {
                        IconButton(onClick = onEliminar) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar Marcador", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                    IconButton(
                        onClick = {
                            if (pasaje.isNotBlank()) {
                                onGuardar(
                                    MarcadorModelo(
                                        id = marcadorInicial?.id ?: System.currentTimeMillis().toString(),
                                        pasaje = pasaje,
                                        nota = nota
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
            OutlinedTextField(
                value = pasaje,
                onValueChange = { pasaje = it },
                label = { Text("Pasaje bíblico (Ej. Juan 3:16)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                label = { Text("Nota de estudio o comentario") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaAjustesCompleta(
    modoTema: ModoTema,
    onCambiarTema: (ModoTema) -> Unit,
    esVistaCuadricula: Boolean,
    onToggleVistaDefault: (Boolean) -> Unit,
    mostrarVistaPrevia: Boolean,
    onToggleVistaPrevia: (Boolean) -> Unit,
    onOpenDrawer: () -> Unit
) {
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
            Text("Opciones de Visualización", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Text("Tema de la Aplicación", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = modoTema == ModoTema.SISTEMA,
                    onClick = { onCambiarTema(ModoTema.SISTEMA) },
                    label = { Text("Sistema") }
                )
                FilterChip(
                    selected = modoTema == ModoTema.CLARO,
                    onClick = { onCambiarTema(ModoTema.CLARO) },
                    label = { Text("Claro") }
                )
                FilterChip(
                    selected = modoTema == ModoTema.OSCURO,
                    onClick = { onCambiarTema(ModoTema.OSCURO) },
                    label = { Text("Oscuro Nocturno") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Preferencias de Notas", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vista Cuadrícula / Mosaico", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Mostrar las notas en dos columnas por defecto", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = esVistaCuadricula,
                    onCheckedChange = onToggleVistaDefault
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Vista Previa de Contenido", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Mostrar el texto de las notas en la pantalla principal", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = mostrarVistaPrevia,
                    onCheckedChange = onToggleVistaPrevia
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text("Acerca de", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("EPPR - Versión 1.0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Plataforma de estudio, notas teológicas y consulta léxica integrada.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
