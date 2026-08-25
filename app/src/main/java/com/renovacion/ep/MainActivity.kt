package com.renovacion.ep

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class NotaKeepModelo(
    val id: String,
    val titulo: String,
    val contenido: String,
    val color: Color
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PantallaNotasPrincipal()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantallaNotasPrincipal() {
    var textoBusqueda by remember { mutableStateOf("") }
    
    val notasEjemplo = remember {
        listOf(
            NotaKeepModelo("1", "Mateo 24:36", "Pero del día y la hora nadie sabe, ni aun los ángeles de los cielos, sino sólo mi Padre.", Color(0xFFFFF4B8)),
            NotaKeepModelo("2", "Idea de Estudio", "Revisar los términos en griego para 'Parusía' en las notas de consulta.", Color(0xFFE6F4EA)),
            NotaKeepModelo("3", "Juan 1:1", "En el principio era el Verbo, y el Verbo era con Dios, y el Verbo era Dios.", Color(0xFFE8F0FE)),
            NotaKeepModelo("4", "Génesis 1:1", "En el principio creó Dios los cielos y la tierra.", Color(0xFFF3E8FD)),
            NotaKeepModelo("5", "Recordatorio", "Agregar referencias faltantes del manuscrito Masorético.", Color(0xFFFCE8E6))
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
            
            // Barra superior estilo Google Keep
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
                    IconButton(onClick = {}) {
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

            // Grilla con las tarjetas de notas
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalItemSpacing = 8.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(notasFiltradas) { nota ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = nota.color)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = nota.titulo,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = nota.contenido,
                                fontSize = 13.sp,
                                color = Color(0xFF3C4043)
                            )
                        }
                    }
                }
            }
        }
    }
}
