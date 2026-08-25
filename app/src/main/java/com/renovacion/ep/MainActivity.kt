@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaMarcadores(
    listaMarcadores: MutableList<MarcadorModelo>,
    onOpenDrawer: () -> Unit
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
                marcadorEnEdicion = null
                esNuevoMarcador = false
            },
            onEliminar = {
                marcadorEnEdicion?.let { m -> listaMarcadores.removeAll { it.id == m.id } }
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
