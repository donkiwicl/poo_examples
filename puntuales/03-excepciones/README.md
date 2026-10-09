# 03 · Excepciones — Movimientos de la Cuenta Kiwi

**Tema:** excepciones comprobadas y no comprobadas, excepciones propias, jerarquía de excepciones, `try/catch/finally`, *multi-catch* ordenado, `try-with-resources`, propagación con `throws`.
**Tipo:** consola · **Tiempo estimado:** 1 bloque

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

Banco Kiwi procesa cada noche un archivo de texto con los movimientos de una cuenta. Hoy, si una línea viene mal escrita, el programa se cae y los movimientos siguientes no se aplican. Se necesita un procesador que:

- distinga una **regla de negocio rechazada** (saldo insuficiente, límite diario superado) de un **dato mal escrito** (`GIRAR abc`, comando desconocido);
- **nunca se detenga** por una línea mala;
- informe con claridad si el archivo no existe.

Reglas de la cuenta:

| Regla | Excepción |
|---|---|
| Monto `<= 0` | `IllegalArgumentException` (error de quien llama) |
| Giro mayor que el saldo | `SaldoInsuficienteException` (incluye cuánto falta) |
| Lo girado en el día superaría **$200.000** | `LimiteDiarioExcedidoException` (incluye cuánto queda disponible) |

## Código inicial

| Archivo | Estado |
|---|---|
| `OperacionRechazadaException.java` | Resuelto. Excepción **comprobada** base de las reglas de negocio. |
| `CuentaBancaria.java` | `depositar` resuelto como ejemplo. **`girar` y `transferir` por completar.** |
| `ProcesadorMovimientos.java` | **`procesar` y `procesarArchivo` por completar.** |
| `Main.java` | Resuelto. Muestra cómo distinguir `NoSuchFileException` de otra `IOException`. |
| `movimientos.txt` | Archivo de entrada de ejemplo. |
| `src/test/...` | Pruebas de autoevaluación. **No compilan hasta que crees las dos excepciones.** |

## Requerimientos

**R1. Excepciones propias.** Crea `SaldoInsuficienteException` y `LimiteDiarioExcedidoException`, ambas subclases de `OperacionRechazadaException`. Cada una recibe el dato numérico en su constructor, arma su propio mensaje y lo expone con un getter (`getFaltante()`, `getDisponibleHoy()`):
- `Saldo insuficiente: faltan $30.000`
- `Supera el limite diario de $200.000: puedes girar hasta $20.000 hoy`

**R2. `girar(int monto)`.** Aplica las reglas en el orden de la tabla. Agrega `throws` a la firma. Si una regla falla, **el saldo no cambia**.

**R3. `transferir(CuentaBancaria destino, int monto)`.** Un destino `null` o la misma cuenta es `IllegalArgumentException`. Si el giro falla, el destino no recibe nada.

**R4. `procesar(String linea)`.** Ejecuta un comando (`DEPOSITAR n`, `GIRAR n`, `SALDO`) y retorna `OK | ...`, `RECHAZADO | ...` o `ERROR | ...`. **Nunca lanza excepciones.** Cuenta cada línea en `finally`.

**R5. `procesarArchivo(Path)`.** Lee con `try-with-resources`, ignora líneas vacías y comentarios (`#`). **No captura** la `IOException`: la propaga a `Main`.

## Resultado esperado

```
OK | DEPOSITAR 50000 | Saldo: $550.000
OK | GIRAR 30000 | Saldo: $520.000
ERROR | GIRAR abc | 'abc' no es un monto valido
RECHAZADO | GIRAR 900000 | Saldo insuficiente: faltan $380.000
OK | GIRAR 150000 | Saldo: $370.000
RECHAZADO | GIRAR 60000 | Supera el limite diario de $200.000: puedes girar hasta $20.000 hoy
ERROR | DEPOSITAR -500 | El monto debe ser mayor que 0
ERROR | VOLAR 10 | Comando desconocido: VOLAR
ERROR | GIRAR | Falta el monto
OK | SALDO | Saldo: $370.000
Lineas procesadas: 10 | Saldo final: $370.000
```

Con un archivo inexistente (`mvn compile exec:java -Dexec.args="no-existe.txt"`):

```
No existe el archivo /ruta/al/proyecto/no-existe.txt
Lineas procesadas: 0 | Saldo final: $500.000
```

## Cómo ejecutar

```bash
mvn compile exec:java
mvn test
```

## Preguntas para pensar

1. ¿Por qué `SaldoInsuficienteException` es comprobada y la de monto negativo no?
2. `NumberFormatException` es subclase de `IllegalArgumentException`. ¿Qué pasa si las capturas en el orden inverso?
3. ¿Qué ventaja tiene que `procesarArchivo` **no** capture la `IOException`?
