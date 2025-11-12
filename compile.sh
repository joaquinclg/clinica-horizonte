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

# Buscar el driver de MySQL (cualquier versión)
MYSQL_DRIVER=$(find lib -name "mysql-connector-j-*.jar" 2>/dev/null | head -n 1)

if [ -z "$MYSQL_DRIVER" ]; then
    echo -e "${RED}⚠ Advertencia: No se encuentra el driver de MySQL${NC}"
    echo "  Descárgalo desde: https://dev.mysql.com/downloads/connector/j/"
    echo "  Guárdalo en: lib/mysql-connector-j-*.jar"
    read -p "¿Continuar compilando sin el driver? (s/n): " respuesta
    if [ "$respuesta" != "s" ]; then
        rm sources.txt
        exit 1
    fi
    MYSQL_DRIVER=""
else
    echo "✓ Driver encontrado: $(basename $MYSQL_DRIVER)"
fi

# Compilar (incluir driver en classpath si existe)
echo "Compilando..."
if [ -n "$MYSQL_DRIVER" ]; then
    javac -d bin --release 17 -cp "$MYSQL_DRIVER" @sources.txt
else
    javac -d bin --release 17 @sources.txt
fi

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