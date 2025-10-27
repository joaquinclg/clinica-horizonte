#!/bin/bash
# Script para compilar el proyecto Clínica Horizonte

echo "=================================="
echo "Compilando Clínica Horizonte..."
echo "=================================="

# Colores para output
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Crear directorio de salida si no existe
if [ ! -d "bin" ]; then
    mkdir bin
    echo "Directorio bin/ creado"
fi

# Encontrar todos los archivos .java
echo "Buscando archivos fuente..."
find . -name "*.java" -not -path "*/bin/*" > sources.txt

# Contar archivos
NUM_FILES=$(wc -l < sources.txt)
echo "Encontrados $NUM_FILES archivos .java"

# Compilar
echo "Compilando..."
javac -d bin --release 17 @sources.txt

# Verificar resultado
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Compilación exitosa${NC}"
    echo ""
    echo "Para ejecutar el proyecto, use:"
    echo "  java -cp bin app.MainDemo"
    echo ""
    echo "O ejecute: ./run.sh"
    rm sources.txt
    exit 0
else
    echo -e "${RED}✗ Error en la compilación${NC}"
    rm sources.txt
    exit 1
fi

