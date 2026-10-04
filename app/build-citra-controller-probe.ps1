$ErrorActionPreference = 'Stop'
$taskRoot = Split-Path $PSScriptRoot
$taskJava = 'C:/Program Files/Android/Android Studio/jbr/bin'
$taskTools = 'C:/Users/sclob/AppData/Local/Android/Sdk/build-tools/36.1.0'
$taskApktool = Join-Path $taskRoot 'research/apktool_3.0.3.jar'
$taskInput = Join-Path $taskRoot 'downloads/Citra_MMJ_20251112.apk'
$taskDecode = Join-Path $taskRoot 'research/citra-controller-probe'
$taskBuild = Join-Path $PSScriptRoot 'build'
$taskUtf8 = New-Object System.Text.UTF8Encoding($false)
& "$taskJava/java.exe" -jar $taskApktool d -f $taskInput -o $taskDecode
if ($LASTEXITCODE) { throw 'Citra decode failed' }
$taskManifestPath = Join-Path $taskDecode 'AndroidManifest.xml'
$taskManifest = [IO.File]::ReadAllText($taskManifestPath)
$taskOldConfig = 'android:configChanges="orientation|screenLayout|screenSize|smallestScreenSize"'
if (($taskManifest.Split([string[]]@($taskOldConfig), [StringSplitOptions]::None).Length - 1) -ne 1) {
    throw 'Expected exactly one emulation config declaration'
}
$taskManifest = $taskManifest.Replace($taskOldConfig, 'android:configChanges="orientation|screenLayout|screenSize|smallestScreenSize|keyboard|keyboardHidden|navigation"')
$taskManifest = $taskManifest.Replace('package="org.citra.emu"', 'package="org.citra.rgn"')
$taskManifest = $taskManifest.Replace('android:label="Citra"', 'android:label="Citra ReGene Probe"')
$taskManifest = $taskManifest.Replace('org.citra.emu.filesprovider', 'org.citra.rgn.filesprovider')
$taskManifest = $taskManifest.Replace('org.citra.emu.userpathprovider', 'org.citra.rgn.userpathprovider')
[IO.File]::WriteAllText($taskManifestPath, $taskManifest, $taskUtf8)
# Keep Java/JNI class names intact. Only the shared user directory is isolated.
$taskPathSmali = Join-Path $taskDecode 'smali/d3/a.smali'
$taskSmali = [IO.File]::ReadAllText($taskPathSmali)
if (($taskSmali.Split([string[]]@('"citra-emu"'), [StringSplitOptions]::None).Length - 1) -ne 1) {
    throw 'Expected exactly one shared user directory literal'
}
[IO.File]::WriteAllText($taskPathSmali, $taskSmali.Replace('"citra-emu"', '"citra-rgn"'), $taskUtf8)
& "$taskJava/java.exe" -jar $taskApktool b $taskDecode -o "$taskBuild/citra-controller-unsigned.apk"
if ($LASTEXITCODE) { throw 'Citra rebuild failed' }
& "$taskTools/zipalign.exe" -f 4 "$taskBuild/citra-controller-unsigned.apk" "$taskBuild/citra-controller-aligned.apk"
if ($LASTEXITCODE) { throw 'Citra alignment failed' }
& "$taskTools/apksigner.bat" sign --ks "$taskBuild/debug.keystore" --ks-key-alias regene --ks-pass pass:android --key-pass pass:android --out "$taskBuild/citra-controller-probe.apk" "$taskBuild/citra-controller-aligned.apk"
if ($LASTEXITCODE) { throw 'Citra signing failed' }
& "$taskTools/apksigner.bat" verify --verbose "$taskBuild/citra-controller-probe.apk"
if ($LASTEXITCODE) { throw 'Citra signature verification failed' }
Get-FileHash "$taskBuild/citra-controller-probe.apk" -Algorithm SHA256
