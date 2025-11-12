#!/bin/bash
# Script para ejecutar el proyecto Clínica Horizonte

echo "=================================="
echo "Clínica Horizonte - Sistema de Gestión de Stock"
echo "=================================="
echo ""

# Colores
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# Verificar si existe el directorio bin
if [ ! -d "bin" ]; then
    echo -e "${RED}Error: No se encuentra el directorio bin/${NC}"
    echo "Debe compilar el proyecto primero:"
    echo "  ./compile.sh"
    exit 1
fi

# Verificar si existe la clase principal
if [ ! -f "bin/app/MainDemo.class" ]; then
    echo -e "${RED}Error: No se encuentra la clase principal compilada${NC}"
    echo "Debe compilar el proyecto primero:"
    echo "  ./compile.sh"
    exit 1
fi

# Buscar el driver de MySQL (cualquier versión)
MYSQL_DRIVER=$(find lib -name "mysql-connector-j-*.jar" 2>/dev/null | head -n 1)

if [ -z "$MYSQL_DRIVER" ]; then
    echo -e "${RED}✗ Error: No se encuentra el driver de MySQL${NC}"
    echo "  No se encontró ningún archivo mysql-connector-j-*.jar en lib/"
    echo ""
    echo "  Descárgalo desde: https://dev.mysql.com/downloads/connector/j/"
    exit 1
fi

echo "✓ Driver encontrado: $(basename $MYSQL_DRIVER)"

# Ejecutar (incluir driver en classpath)
echo -e "${GREEN}Iniciando aplicación...${NC}"
echo ""
java -cp "bin:$MYSQL_DRIVER" app.MainDemo