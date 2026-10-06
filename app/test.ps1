param([string]$JavaBin='C:/Program Files/Android/Android Studio/jbr/bin')
$ErrorActionPreference='Stop'
$taskOutput=Join-Path $PSScriptRoot 'build/tests'
New-Item -ItemType Directory -Force $taskOutput | Out-Null
$taskSources=@('RotationPolicy','ForegroundHistory','CitraTargets','ReleaseActions','ContentProfile') | ForEach-Object {
    Join-Path $PSScriptRoot "src/dev/regene/v50s/$_.java"
}
$taskTests=@(Get-ChildItem (Join-Path $PSScriptRoot 'tests/dev/regene/v50s') -Filter '*.java')
& "$JavaBin/javac.exe" --release 8 -d $taskOutput @taskSources @($taskTests.FullName)
if($LASTEXITCODE){throw 'Test compilation failed'}
foreach($taskTest in $taskTests){
    & "$JavaBin/java.exe" -cp $taskOutput "dev.regene.v50s.$($taskTest.BaseName)"
    if($LASTEXITCODE){throw "Failed: $($taskTest.BaseName)"}
}
# Apktool may emit LF or CRLF. Extract the real build expression and verify
# that the injected v0 register is allocated with either newline convention.
$taskAst=[System.Management.Automation.Language.Parser]::ParseFile(
    (Join-Path $PSScriptRoot 'build-citra-controller-probe.ps1'),[ref]$null,[ref]$null)
$taskAssignment=$taskAst.Find({param($node)
    $node -is [System.Management.Automation.Language.AssignmentStatementAst] -and
    $node.Left.Extent.Text -eq '$taskLocalsPattern'
},$true)
$taskLiteral=$taskAssignment.Right.Find({param($node)
    $node -is [System.Management.Automation.Language.StringConstantExpressionAst]
},$true)
if(!$taskLiteral){throw 'Surface register pattern must be a literal'}
$taskPattern=$taskLiteral.Value
foreach($taskNewline in @("`n","`r`n")){
    $taskInput=".method public surfaceChanged(Landroid/view/SurfaceHolder;III)V${taskNewline}    .locals 0${taskNewline}.end method"
    if([regex]::Matches($taskInput,$taskPattern).Count -ne 1){throw 'Missing surface register declaration'}
    $taskResult=[regex]::Replace($taskInput,$taskPattern,'${1}1')
    if(!$taskResult.Contains('    .locals 1')){throw 'Surface callback register not allocated'}
}
Write-Output 'Citra LF/CRLF surface-register tests passed'
