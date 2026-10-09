# 02 · Polimorfismo — Medios de pago del Almacén Don Kiwi

**Tema:** clases abstractas, métodos abstractos, interfaces, despacho dinámico, `instanceof` con patrón.
**Tipo:** consola · **Tiempo estimado:** 1 bloque

> La guía de resolución está en la rama `solucion`, en el archivo `GUIA.md` de esta carpeta.

## Problema

El Almacén Don Kiwi acepta varios medios de pago y cada uno calcula el total de forma distinta:

| Medio | Total a pagar | Puntos |
|---|---|---|
| **Efectivo** | Redondeado a la decena: si termina en 1-5 baja y si termina en 6-9 sube. | No |
| **Débito** | El monto exacto. | No |
| **Crédito** (1 a 12 cuotas) | Recargo de **1,5 %** por cada cuota después de la primera. | 1 punto por cada $100 pagados |

La caja debe poder cobrar con **cualquier** medio de pago, incluidos los que se agreguen en el futuro, **sin modificar su código** y **sin preguntar de qué clase es** cada medio.

## Código inicial

| Archivo | Estado |
|---|---|
| `MedioPago.java` | Resuelto. Clase **abstracta** con `calcularTotal(int)` y `getNombre()` abstractos. |
| `Efectivo.java` | Resuelto. Úsalo como ejemplo. |
| `Acumulable.java` | Resuelto. Interfaz para los medios que otorgan puntos. |
| `Formato.java` | Resuelto. `Formato.pesos(12345)` → `"$12.345"`. |
| `Caja.java` | **Por completar** (`cobrar`). |
| `Main.java` | Tiene líneas comentadas que debes activar. |
| `src/test/.../CajaTest.java` | Pruebas de autoevaluación. **No compilan hasta que crees las tarjetas.** |

## Requerimientos

**R1. Clase abstracta.** Intenta escribir `new MedioPago("Ana")` en `Main`. ¿Qué dice el compilador? Bórralo y explica en un comentario por qué tiene sentido.

**R2. `TarjetaDebito` y `TarjetaCredito`**
- Ambas heredan de `MedioPago`. `getNombre()` retorna `"Debito"`, `"Credito 1 cuota"` o `"Credito N cuotas"`.
- `TarjetaCredito` recibe las cuotas en el constructor (1 a 12, si no `IllegalArgumentException`) e **implementa `Acumulable`**.

**R3. `Caja.cobrar(int monto, MedioPago medio)`**
- Rechaza montos `<= 0` con `IllegalArgumentException`.
- Calcula el total con el medio de pago, lo suma al total recaudado y arma la línea de boleta:
  `Credito 3 cuotas | Carla Soto | Compra: $60.000 | Total: $61.800 | Puntos: 618`
- La parte `| Puntos: N` aparece solo si el medio es `Acumulable`. **No se permite** `instanceof Efectivo`, `instanceof TarjetaCredito` ni `getClass()`.

**R4. Desafío: un medio nuevo.** Crea `Transferencia` (comisión fija de $300 y 2 puntos por cada $100) **sin tocar `Caja`**.

## Resultado esperado

```
Efectivo | Ana Rojas | Compra: $12.345 | Total: $12.340
Debito | Bruno Diaz | Compra: $8.990 | Total: $8.990
Credito 3 cuotas | Carla Soto | Compra: $60.000 | Total: $61.800 | Puntos: 618
Efectivo | Diego Pinto | Compra: $4.996 | Total: $5.000
Transferencia | Elena Mora | Compra: $25.000 | Total: $25.300 | Puntos: 506
Total recaudado: $113.430
Puntos otorgados: 1124
```

## Cómo ejecutar

```bash
mvn compile exec:java
mvn test
```

## Preguntas para pensar

1. ¿Por qué `Acumulable` es una interfaz y no un método `calcularPuntos()` en `MedioPago`?
2. Si en `Caja` usaras `if (medio instanceof Efectivo) ... else if (medio instanceof TarjetaDebito) ...`, ¿qué tendrías que cambiar al agregar `Transferencia`?
3. ¿Qué diferencia hay entre una clase abstracta y una interfaz? ¿Podría `MedioPago` ser una interfaz?
