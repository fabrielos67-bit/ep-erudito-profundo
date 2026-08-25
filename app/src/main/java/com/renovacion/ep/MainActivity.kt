@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaMarcadores(
    listaMarcadores: MutableList<MarcadorModelo>,
    onOpenDrawer: () -> Unit
) {
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

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
                onClick = { mostrarDialogoCrear = true },
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
                Card(modifier = Modifier.fillMaxWidth()) {
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
                        Column {
                            Text(text = marcador.pasaje, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = marcador.nota, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoCrear) {
        DialogoMarcador(
            onDismiss = { mostrarDialogoCrear = false },
            onGuardar = { nuevo ->
                listaMarcadores.add(0, nuevo)
                mostrarDialogoCrear = false
            }
        )
    }
}

@Composable
private fun DialogoMarcador(
    onDismiss: () -> Unit,
    onGuardar: (MarcadorModelo) -> Unit
) {
    var pasaje by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Marcador") },
        text = {
            Column {
                OutlinedTextField(
                    value = pasaje,
                    onValueChange = { pasaje = it },
                    label = { Text("Pasaje bíblico (Ej. Juan 3:16)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nota,
                    onValueChange = { nota = it },
                    label = { Text("Nota de estudio o comentario") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (pasaje.isNotBlank()) {
                        onGuardar(
                            MarcadorModelo(
                                id = System.currentTimeMillis().toString(),
                                pasaje = pasaje,
                                nota = nota
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
