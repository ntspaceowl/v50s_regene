param([string]$Adb='C:/Users/sclob/AppData/Local/Android/Sdk/platform-tools/adb.exe')
$ErrorActionPreference='Stop'
# Apply while these three emulators are inactive. Keep three pixels clear on
# either side of the 1080-pixel panel seam, with exact 5:3 / 4:3 ratios.
$profiles=@(
 @{Package='org.citra.rgn';Path='/sdcard/citra-rgn/config/config-mmj.ini';Values=@{
 landscape_top_left=272;landscape_top_top=0;landscape_top_right=2067;landscape_top_bottom=1077
 landscape_bottom_left=452;landscape_bottom_top=1083;landscape_bottom_right=1888;landscape_bottom_bottom=2160}},
 @{Package='org.azahar_emu.azahar.regeneprobe';Path='/sdcard/AzaharReGeneProbe/config/config.ini';Values=@{
 custom_top_x=272;custom_top_y=0;custom_top_width=1795;custom_top_height=1077
 custom_bottom_x=452;custom_bottom_y=1083;custom_bottom_width=1436;custom_bottom_height=1077}}
)
$activity=(& $Adb shell dumpsys activity activities | Select-String mResumedActivity | Select-Object -First 1)
if(!$activity){throw 'Cannot determine foreground activity'}
foreach($package in @($profiles.Package)+@('me.magnum.melonds.regeneprobe')){
 if($activity.ToString().Contains($package)){throw "Exit $package before applying profiles"}
}
$backup=Join-Path $PSScriptRoot '../backups/2026-10-04/panel-seam'
New-Item -ItemType Directory -Force $backup | Out-Null
foreach($profile in $profiles){
 & $Adb shell am force-stop $profile.Package
 $config=(& $Adb shell cat $profile.Path) -join "`n"
 if($LASTEXITCODE -ne 0){throw "Cannot read $($profile.Path)"}
 $saved=Join-Path $backup ($profile.Package+'.ini')
 if(!(Test-Path -LiteralPath $saved)){[IO.File]::WriteAllText($saved,$config)}
 foreach($key in $profile.Values.Keys){
  $pattern='(?m)^'+[regex]::Escape($key)+'\s*=.*$'
  if($config -notmatch $pattern){throw "Missing setting: $key"}
  $config=[regex]::Replace($config,$pattern,"$key = $($profile.Values[$key])")
 }
 $config | & $Adb shell tee $profile.Path | Out-Null
 if($LASTEXITCODE -ne 0){throw "Cannot write $($profile.Path)"}
 Write-Output "$($profile.Package): panel seam profile applied"
}
$package='me.magnum.melonds.regeneprobe'
& $Adb shell am force-stop $package
$json=(& $Adb shell run-as $package cat files/layouts.json) -join "`n"
if($LASTEXITCODE -ne 0){throw 'Cannot read melonDS layouts'}
$saved=Join-Path $backup 'melonds-layouts.json'
if(!(Test-Path -LiteralPath $saved)){[IO.File]::WriteAllText($saved,$json)}
$layouts=ConvertFrom-Json $json
$layout=$layouts | Where-Object id -EQ '7968f05c-b5ed-4f98-a9b4-7256b4f0a178'
if(!$layout){throw 'ReGene melonDS layout missing'}
foreach($variant in $layout.layoutVariants){
 if($variant.variant.uiSize.y -ne 2160){continue}
 foreach($component in $variant.layout.mainScreenLayoutDto.components){
  if($component.component -notin @('TOP_SCREEN','BOTTOM_SCREEN')){continue}
  $component.rect.width=1436; $component.rect.height=1077; $component.rect.x=414
  $component.rect.y=if($component.component -eq 'TOP_SCREEN'){0}else{1083}
 }
}
ConvertTo-Json -InputObject @($layouts) -Depth 30 -Compress | & $Adb shell run-as $package tee files/layouts.json | Out-Null
if($LASTEXITCODE -ne 0){throw 'Cannot write melonDS layouts'}
Write-Output 'melonDS: panel seam profile applied; button positions unchanged'
