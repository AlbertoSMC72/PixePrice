package com.example.pixelprice.features.views.quoteResult.precentation

@Composable
fun QuoteResultScreen(
    viewModel: QuoteViewModel,
    projectId: Int,
    onBack: () -> Unit
) {
    val quote by viewModel.quote.observeAsState()
    val error by viewModel.error.observeAsState("")

    LaunchedEffect(Unit) {
        viewModel.loadQuote(projectId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Teal)
    ) {
        Text(
            text = "Cotización Generada",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Beige,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (quote != null) {
            Text("Proyecto: ${quote!!.projectName}", color = Beige, fontSize = 18.sp)
            Text("Fecha: ${quote!!.createdAt}", color = Beige, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Costo estimado:", color = Beige, fontWeight = FontWeight.SemiBold)
            Text("$${quote!!.estimatedCost}", color = Beige, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Resumen:", color = Beige, fontWeight = FontWeight.SemiBold)
            Text(quote!!.summary, color = Beige)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onBack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Beige,
                    contentColor = Teal
                )
            ) {
                Text("Volver", fontWeight = FontWeight.Bold)
            }

        } else if (error.isNotEmpty()) {
            Text(text = error, color = Coral)
        } else {
            CircularProgressIndicator(color = Beige)
        }
    }
}
