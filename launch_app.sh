#!/bin/bash

# Script interactivo para lanzar la app Android (Debug/Release)
# Autor: LeandroLCD

# Valores por defecto
BUILD_TYPE="debug"
GRADLE_TASK="assembleDebug"
APK_NAME="app-debug.apk"
SHOW_LOGS=true

# Procesar parámetros
if [ "$1" == "-release" ]; then
    BUILD_TYPE="release"
    GRADLE_TASK="assembleRelease"
    APK_NAME="app-release-unsigned.apk"
    SHOW_LOGS=false
    echo "⚠️  Modo RELEASE activado (no se mostrarán logs)"
fi

echo "------------------------------------------"
echo "🔍 Buscando dispositivos conectados..."
echo "------------------------------------------"

mapfile -t DEVICES < <(adb devices | grep -v "List of devices attached" | grep "device$" | cut -f1)
MAP_COUNT=${#DEVICES[@]}

if [ $MAP_COUNT -eq 0 ]; then
    echo "❌ Error: No hay dispositivos o emuladores conectados."
    exit 1
fi

SELECTED_DEVICE=""
if [ $MAP_COUNT -eq 1 ]; then
    SELECTED_DEVICE=${DEVICES[0]}
    echo "📱 Un solo dispositivo detectado: $SELECTED_DEVICE"
else
    echo "📱 Se detectaron $MAP_COUNT dispositivos:"
    for i in "${!DEVICES[@]}"; do
        MODEL=$(adb -s "${DEVICES[$i]}" shell getprop ro.product.model)
        echo "$((i+1))) ID: ${DEVICES[$i]} | Modelo: $MODEL"
    done
    echo "------------------------------------------"
    read -p "👉 Selecciona el número (1-$MAP_COUNT): " SELECTION
    if [[ "$SELECTION" =~ ^[0-9]+$ ]] && [ "$SELECTION" -ge 1 ] && [ "$SELECTION" -le "$MAP_COUNT" ]; then
        SELECTED_DEVICE=${DEVICES[$((SELECTION-1))]}
    else
        echo "❌ Selección inválida."
        exit 1
    fi
fi

echo "🚀 Compilando en modo $BUILD_TYPE..."
echo "------------------------------------------"

if ./gradlew "$GRADLE_TASK"; then
    APK_PATH="app/build/outputs/apk/$BUILD_TYPE/$APK_NAME"
    
    # Verificar si el APK existe (por si el nombre cambia en release)
    if [ ! -f "$APK_PATH" ] && [ "$BUILD_TYPE" == "release" ]; then
        # Buscar cualquier apk en la carpeta release si el esperado no existe
        APK_PATH=$(ls app/build/outputs/apk/release/*.apk | head -n 1)
    fi

    echo "📦 Instalando $APK_PATH en $SELECTED_DEVICE..."
    if adb -s "$SELECTED_DEVICE" install -r "$APK_PATH"; then
        echo "✅ Instalación exitosa."
        echo "🎬 Arrancando MainActivity..."
        adb -s "$SELECTED_DEVICE" shell am start -n com.aristidevs.cursotestingandroid/.MainActivity
        
        if [ "$SHOW_LOGS" = true ]; then
            echo "------------------------------------------"
            echo "📖 Iniciando Logcat (Presiona Ctrl+C para detener)..."
            echo "------------------------------------------"
            # Limpiar logs previos para ver solo los nuevos
            adb -s "$SELECTED_DEVICE" logcat -c
            # Mostrar logs filtrando por el paquete de la app
            adb -s "$SELECTED_DEVICE" logcat --pid=$(adb -s "$SELECTED_DEVICE" shell pidof -s com.aristidevs.cursotestingandroid)
        fi
    else
        echo "❌ Error al instalar."
    fi
else
    echo "❌ Error en la compilación."
    exit 1
fi


