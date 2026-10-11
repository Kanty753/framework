#!/bin/bash

echo "=================================="
echo "  BUILD MINI FRAMEWORK PROJECT"
echo "=================================="

JAR_NAME="framework.jar"

SRC_DIR="./src"
APP_DIR="."

APP_LIB="./WEB-INF/lib"
APP_CLASSES="./WEB-INF/classes"

TOMCAT_HOME="/home/aina/Documents/ITU/S3/TOMCAT"
DEPLOY_DIR="$TOMCAT_HOME/webapps/app-test"

JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac))))
export JAVA_HOME
export PATH=$JAVA_HOME/bin:$PATH

echo ""
echo "[1] Vérification du framework..."

if [ ! -f "$APP_LIB/$JAR_NAME" ]; then
    echo "❌ $JAR_NAME introuvable dans $APP_LIB"
    exit 1
fi

echo "✅ Framework trouvé"

echo ""
echo "[2] Préparation des dossiers..."

mkdir -p "$APP_CLASSES"

echo ""
echo "[3] Compilation des contrôleurs..."

find "$SRC_DIR" -name "*.java" > sources.txt

if [ ! -s sources.txt ]; then
    echo "❌ Aucun fichier Java trouvé"
    rm -f sources.txt
    exit 1
fi

GSON_JAR="$APP_LIB/gson-2.11.0.jar"

javac \
-parameters \
-cp "$APP_LIB/$JAR_NAME:$GSON_JAR:$TOMCAT_HOME/lib/servlet-api.jar" \
-d "$APP_CLASSES" \
@sources.txt

if [ $? -ne 0 ]; then
    echo "❌ Erreur compilation"
    rm -f sources.txt
    exit 1
fi

rm -f sources.txt

echo "✅ Compilation terminée"

echo ""
echo "[4] Déploiement Tomcat..."

rm -rf "$DEPLOY_DIR"
mkdir -p "$DEPLOY_DIR"

cp -r "$APP_DIR"/* "$DEPLOY_DIR"/

if [ $? -ne 0 ]; then
    echo "❌ Erreur déploiement"
    exit 1
fi

echo "✅ Déploiement terminé"

echo ""
echo "[5] Redémarrage Tomcat..."

$TOMCAT_HOME/bin/shutdown.sh
sleep 3
$TOMCAT_HOME/bin/startup.sh

echo ""
echo "=================================="
echo "✅ BUILD + DEPLOY OK"
echo "=================================="

echo ""
echo "URL :"
echo "http://localhost:8080/app-test/"