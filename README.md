# CallScribe

Application Android Kotlin pour enregistrer l'audio ambiant pendant un appel via le microphone physique, afin de produire un fichier WAV mono 16 kHz exploitable pour transcription.

## Build

Prerequis locaux:

- JDK 17
- Android SDK installe dans `C:\Users\user\Android\Sdk`

Commande:

```powershell
$env:ANDROID_HOME='C:\Users\user\Android\Sdk'
$env:ANDROID_SDK_ROOT='C:\Users\user\Android\Sdk'
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

APK genere:

- `app/build/outputs/apk/debug/app-debug.apk`

## Ce que l'application fait

- demande proprement les permissions runtime utiles
- surveille l'etat d'appel
- tente d'activer le haut-parleur
- enregistre le micro via `AudioRecord`
- ecrit un vrai fichier WAV
- conserve l'historique dans Room
- permet lecture, partage, suppression, renommage
- expose une calibration simple et des logs de diagnostic
- peut garder l'ecran allume selon le reglage utilisateur
- peut tenter un auto-start/auto-stop sur changement d'etat d'appel tant que le process est vivant

## Limites reelles

- pas de capture native du flux telephonique systeme
- resultat dependant du haut-parleur, du volume et du constructeur
- qualite variable selon le micro et la ROM
- l'auto-start sur appel n'est pas une garantie systeme universelle; il depend des restrictions de fond du constructeur et du fait que le process soit actif
- verification sur appareil reel non incluse dans ce workspace

## Points d'entree utiles

- Application et DI: `app/src/main/java/com/personal/callscribe/CallScribeApp.kt`
- Session manager: `app/src/main/java/com/personal/callscribe/service/session/RecordingSessionManager.kt`
- Foreground service: `app/src/main/java/com/personal/callscribe/service/RecordingForegroundService.kt`
- Navigation UI: `app/src/main/java/com/personal/callscribe/presentation/navigation/NavGraph.kt`
