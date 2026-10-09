package cl.dsy1102.ejemplos.rut;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Estas pruebas NO compilan hasta que agregues JUnit al pom.xml (R3):
 * "package org.junit.jupiter.api does not exist".
 */
class ValidadorRutTest {

    @Test
    void calculaElDigitoVerificador() {
        assertEquals('5', ValidadorRut.calcularDigitoVerificador(12_345_678));
        assertEquals('1', ValidadorRut.calcularDigitoVerificador(11_111_111));
        assertEquals('K', ValidadorRut.calcularDigitoVerificador(10_000_013));
        assertEquals('0', ValidadorRut.calcularDigitoVerificador(10_000_004));
    }

    @Test
    void aceptaDistintosFormatos() {
        assertTrue(ValidadorRut.esValido("12.345.678-5"));
        assertTrue(ValidadorRut.esValido("12345678-5"));
        assertTrue(ValidadorRut.esValido("123456785"));
    }

    @Test
    void rechazaRutInvalidos() {
        assertFalse(ValidadorRut.esValido("12.345.678-9"));
        assertFalse(ValidadorRut.esValido("abc"));
        assertFalse(ValidadorRut.esValido(""));
        assertFalse(ValidadorRut.esValido(null));
    }

    @Test
    void formatea() {
        assertEquals("12.345.678-5", ValidadorRut.formatear("123456785"));
        assertEquals("10.000.013-K", ValidadorRut.formatear("10000013k"));
        assertThrows(IllegalArgumentException.class, () -> ValidadorRut.formatear("1-1"));
    }
}
