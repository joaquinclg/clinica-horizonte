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

# Ejecutar
echo -e "${GREEN}Iniciando aplicación...${NC}"
echo ""
java -cp bin app.MainDemo

