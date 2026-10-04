$ErrorActionPreference = 'Stop'
$taskApp = $PSScriptRoot
$taskSdk = 'C:/Users/sclob/AppData/Local/Android/Sdk'
$taskTools = Join-Path $taskSdk 'build-tools/36.1.0'
$taskJar = Join-Path $taskSdk 'platforms/android-36/android.jar'
$taskJava = 'C:/Program Files/Android/Android Studio/jbr/bin'
$taskBuild = Join-Path $taskApp 'build'
foreach ($taskOutput in @('classes','dex')) {
    $taskOutputPath = [IO.Path]::GetFullPath((Join-Path $taskBuild $taskOutput))
    if (!$taskOutputPath.StartsWith([IO.Path]::GetFullPath($taskBuild) + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {throw 'Output outside build directory'}
    if (Test-Path -LiteralPath $taskOutputPath) {Remove-Item -LiteralPath $taskOutputPath -Recurse -Force}
}
New-Item -ItemType Directory -Force "$taskBuild/classes","$taskBuild/dex" | Out-Null
$taskSources = @(Get-ChildItem "$taskApp/src" -Recurse -Filter '*.java' | ForEach-Object FullName)
& "$taskJava/javac.exe" --release 8 -encoding UTF-8 -classpath $taskJar -d "$taskBuild/classes" @taskSources
if ($LASTEXITCODE) {throw 'javac failed'}
$taskClasses = @(Get-ChildItem "$taskBuild/classes" -Recurse -Filter '*.class' | ForEach-Object FullName)
$env:JAVA_HOME = Split-Path $taskJava
& "$taskTools/d8.bat" --lib $taskJar --min-api 26 --output "$taskBuild/dex" @taskClasses
if ($LASTEXITCODE) {throw 'D8 failed'}
& "$taskTools/aapt.exe" package -f -M "$taskApp/AndroidManifest.xml" -S "$taskApp/res" -I $taskJar -F "$taskBuild/unsigned.apk"
if ($LASTEXITCODE) {throw 'aapt failed'}
Push-Location "$taskBuild/dex"
try { & "$taskTools/aapt.exe" add "$taskBuild/unsigned.apk" classes.dex } finally {Pop-Location}
if ($LASTEXITCODE) {throw 'dex packaging failed'}
& "$taskTools/zipalign.exe" -f 4 "$taskBuild/unsigned.apk" "$taskBuild/aligned.apk"
if ($LASTEXITCODE) {throw 'zipalign failed'}
if (!(Test-Path "$taskBuild/debug.keystore")) {
 & "$taskJava/keytool.exe" -genkeypair -keystore "$taskBuild/debug.keystore" -storepass android -keypass android -alias regene -keyalg RSA -validity 3650 -dname 'CN=ReGene Development'
 if ($LASTEXITCODE) {throw 'key generation failed'}
}
& "$taskTools/apksigner.bat" sign --ks "$taskBuild/debug.keystore" --ks-key-alias regene --ks-pass pass:android --key-pass pass:android --out "$taskBuild/regene-debug.apk" "$taskBuild/aligned.apk"
if ($LASTEXITCODE) {throw 'signing failed'}
& "$taskTools/apksigner.bat" verify --verbose "$taskBuild/regene-debug.apk"
if ($LASTEXITCODE) {throw 'signature verification failed'}
