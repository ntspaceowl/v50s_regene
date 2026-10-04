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
# Re-selecting the same ROM must resume its existing Activity, not start a second native engine.
$taskActivityPath = Join-Path $taskDecode 'smali/org/citra/emu/ui/EmulationActivity.smali'
$taskActivity = [IO.File]::ReadAllText($taskActivityPath)
$taskLaunchPattern = '(?m)^\.method public static n0\(Landroid/content/Context;Lw2/a;\)V\r?\n    \.locals 4'
if ([regex]::Matches($taskActivity, $taskLaunchPattern).Count -ne 1) {
    throw 'Expected exactly one installed ROM launch entry point'
}
$taskResume = @'
.method public static n0(Landroid/content/Context;Lw2/a;)V
    .locals 4

    invoke-static {}, Lorg/citra/emu/ui/EmulationActivity;->j0()Lorg/citra/emu/ui/EmulationActivity;
    move-result-object v0
    if-eqz v0, :regene_new_game
    invoke-virtual {v0}, Landroid/app/Activity;->isFinishing()Z
    move-result v1
    if-nez v1, :regene_new_game
    invoke-virtual {v0}, Landroid/app/Activity;->isDestroyed()Z
    move-result v1
    if-nez v1, :regene_new_game
    # IsRunning is false while paused; the live Activity owns the resumable game.
    iget-object v1, v0, Lorg/citra/emu/ui/EmulationActivity;->v:Ljava/lang/String;
    if-eqz v1, :regene_new_game
    invoke-virtual {p1}, Lw2/a;->e()Ljava/lang/String;
    move-result-object v2
    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :regene_new_game
    new-instance v1, Landroid/content/Intent;
    const-class v2, Lorg/citra/emu/ui/EmulationActivity;
    invoke-direct {v1, p0, v2}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    const v2, 0x20000
    invoke-virtual {v1, v2}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;
    invoke-virtual {p0, v1}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
    const-string v1, "ReGeneCitraProbe"
    const-string v2, "Resumed existing game without another native Run"
    invoke-static {v1, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    return-void

    :regene_new_game
'@
$taskActivity = [regex]::Replace($taskActivity, $taskLaunchPattern, $taskResume)
# Display-added notifications can arrive after a transient display has disappeared.
$taskDisplayPattern = '(?s)(\.method public onDisplayAdded\(I\)V.*?move-result-object p1\r?\n)(\s*\.line 7)'
if ([regex]::Matches($taskActivity, $taskDisplayPattern).Count -ne 1) {
    throw 'Expected exactly one display-added lookup'
}
$taskActivity = [regex]::Replace($taskActivity, $taskDisplayPattern, '$1' + "`n    if-eqz p1, :goto_0`n" + '$2')
[IO.File]::WriteAllText($taskActivityPath, $taskActivity, $taskUtf8)
# The original callback ignores size changes once the game is running. Rebind the
# resized Surface so its native buffer follows portrait -> dual landscape.
$taskSurfacePattern = '(?s)(\.method public surfaceChanged\(Landroid/view/SurfaceHolder;III\)V.*?iput-object p1, p0, Lorg/citra/emu/ui/EmulationActivity;->y:Landroid/view/Surface;)(.*?\.end method)'
if ([regex]::Matches($taskActivity, $taskSurfacePattern).Count -ne 1) { throw 'Expected one surface resize callback' }
$taskSurfaceRefresh = @'

    invoke-static {p1}, Lorg/citra/emu/NativeLibrary;->SurfaceChanged(Landroid/view/Surface;)V
    invoke-static {}, Lorg/citra/emu/NativeLibrary;->IsRunning()Z
    move-result v0
    if-eqz v0, :regene_surface_ready
    invoke-direct {p0}, Lorg/citra/emu/ui/EmulationActivity;->J0()V
    invoke-direct {p0}, Lorg/citra/emu/ui/EmulationActivity;->I0()V
    invoke-static {}, Lorg/citra/emu/NativeLibrary;->WindowChanged()V
    :regene_surface_ready
'@
$taskActivity = [regex]::Replace($taskActivity, $taskSurfacePattern, '$1' + $taskSurfaceRefresh + '$2')
$taskLocalsPattern = '(?m)(^\.method public surfaceChanged\(Landroid/view/SurfaceHolder;III\)V\r?\n    \.locals )0(?=\r?$)'
if ([regex]::Matches($taskActivity, $taskLocalsPattern).Count -ne 1) {throw 'Expected one surface callback register declaration'}
$taskActivity = [regex]::Replace($taskActivity, $taskLocalsPattern, '${1}1')
[IO.File]::WriteAllText($taskActivityPath, $taskActivity, $taskUtf8)
& "$taskJava/java.exe" -jar $taskApktool b $taskDecode -o "$taskBuild/citra-controller-unsigned.apk"
if ($LASTEXITCODE) { throw 'Citra rebuild failed' }
& "$taskTools/zipalign.exe" -f 4 "$taskBuild/citra-controller-unsigned.apk" "$taskBuild/citra-controller-aligned.apk"
if ($LASTEXITCODE) { throw 'Citra alignment failed' }
& "$taskTools/apksigner.bat" sign --ks "$taskBuild/debug.keystore" --ks-key-alias regene --ks-pass pass:android --key-pass pass:android --out "$taskBuild/citra-controller-probe.apk" "$taskBuild/citra-controller-aligned.apk"
if ($LASTEXITCODE) { throw 'Citra signing failed' }
& "$taskTools/apksigner.bat" verify --verbose "$taskBuild/citra-controller-probe.apk"
if ($LASTEXITCODE) { throw 'Citra signature verification failed' }
Get-FileHash "$taskBuild/citra-controller-probe.apk" -Algorithm SHA256
